package ncdsearch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ncdsearch.comparison.ICodeDistanceStrategy;
import ncdsearch.comparison.IVariableWindowStrategy;
import ncdsearch.comparison.TokenSequence;
import ncdsearch.comparison.algorithm.PredictionFilter;
import ncdsearch.eval.FileComparison;
import ncdsearch.files.IFile;
import ncdsearch.files.IFiles;
import ncdsearch.report.Fragment;
import ncdsearch.report.IReport;
import ncdsearch.util.Concurrent;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;


/**
 * This is the main class of NCDSearch. 
 */
public class SearchMain {

	public static final String ARG_TEST_COMPARE = "--test-compare";
	private SearchConfiguration config;
	
	/**
	 * Parse the arguments and execute a search.
	 * @param args is an array of command line arguments
	 */
	public static void main(String[] args) {
		// If the first argument is test comparison, the program directly compares two files.
		if (args.length > 1 && args[0].equals(ARG_TEST_COMPARE)) {
			FileComparison.main(Arrays.copyOfRange(args, 1, args.length));
			return;
		}

		SearchConfiguration config = new SearchConfiguration(args);
		if (config.isTestingConfiguration()) {
			try (IReport report = config.getReport()) {
				// simply output a report
			} catch (IOException e) {
			}
			
			// The config test mode does not proceed to an actual search
			return;
		}

		if (config.isValidConfiguration()) {
			SearchMain instance = new SearchMain(config);
			instance.execute();
		} else {
			System.err.println(config.getArgumentError());
		}
	}

	/**
	 * Create an instance to keep a configuration.
	 * @param config specifies a configuration created by command line arguments
	 */
	public SearchMain(SearchConfiguration config) {
		this.config = config;
	}
	
	/**
	 * Execute a search according to the given configuration
	 */
	public void execute() {
		
		// This is a List to record all strategy objects created for multi-threaded search
		final List<ICodeDistanceStrategy> createdStrategies = Collections.synchronizedList(new ArrayList<ICodeDistanceStrategy>());

		try (IReport report = config.getReport()) {
			
			// A search strategy object is created for each thread 
			ThreadLocal<ICodeDistanceStrategy> strategies = new ThreadLocal<ICodeDistanceStrategy>() {
				@Override
				protected ICodeDistanceStrategy initialValue() {
					ICodeDistanceStrategy similarityStrategy = config.createStrategy();
					createdStrategies.add(similarityStrategy);
					return similarityStrategy;
				}
			};
			
			final Concurrent c = new Concurrent(config.getThreadCount(), null);
			
			try (IFiles files = config.getFiles()) { 

				for (IFile f = files.next(); f != null; f = files.next()) {
					final String targetPath = f.getPath();
					if (config.isVerbose()) System.err.println(targetPath);
					
					final FileType type = config.getTargetLanguage(targetPath);
					if (TokenReaderFactory.isSupported(type)) {
						
						// Analyze the content if the file is a target programming language 
						final IFile target = f;
						c.execute(new Concurrent.Task() {
							@Override
							public boolean run(OutputStream out) throws IOException {
	
								// Read the file content
								TokenReader reader = TokenReaderFactory.create(type, target.read(), config.getSourceCharset());
								TokenSequence fileTokens = new TokenSequence(reader, config.useNormalization(), config.useSeparator());
						
								// Apply a quick filter 
								PredictionFilter prefilter = config.getPrefilter();
								if (prefilter == null || prefilter.shouldSearch(fileTokens)) {
									int[] positions;
									if (config.isFullScan()) {
										positions = fileTokens.getFullPositions(config.getQueryTokens().size());
									} else {
										positions = fileTokens.getLineHeadTokenPositions();
									}
	
									// Identify a similar code fragment for each position (if exists)
									ArrayList<Fragment> fragments = new ArrayList<>();
									ICodeDistanceStrategy similarityStrategy = strategies.get();
									for (int p=0; p<positions.length; p++) {
										Fragment fragment = checkPosition(targetPath, fileTokens, positions[p], similarityStrategy);
										if (fragment != null) {
											fragments.add(fragment);
										}
									}
							
									if (config.allowOverlap()) {
										// Print the raw result
										report.write(fragments);
									} else {
										// Remove redundant elements and print the result.
										ArrayList<Fragment> result = Fragment.filter(fragments);
										if (result.size() > 0) {
											report.write(result);
										}
									}
								}
								return true;
							}
						});
					}
				}
				c.waitComplete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			for (ICodeDistanceStrategy s: createdStrategies) {
				s.close();
			}
		}
	}
	
	/**
	 * Compare source code of a particular position with a query 
	 * @return the best code fragment 
	 */
	private Fragment checkPosition(String filepath, TokenSequence fileTokens, int startPos, ICodeDistanceStrategy similarityStrategy) {
		if (similarityStrategy instanceof IVariableWindowStrategy) {
			// A single call to find the best match 
			IVariableWindowStrategy strategy = (IVariableWindowStrategy)similarityStrategy;
			int endPos = startPos + config.getLargestWindowSize();
			double distance = strategy.findBestMatch(fileTokens, startPos, endPos, config.getDistanceThreshold());
			if (distance <= config.getDistanceThreshold()) {
				int w = strategy.getBestWindowSize();
				return new Fragment(filepath, fileTokens, startPos, startPos+w, distance); 
			} else {
				return null;
			}
		}  else {
			// Try several window size and report the best one
			double minDistance = Double.MAX_VALUE;
			int minWindowSize = -1;
			for (int w=0; w<config.getWindowSizeCount(); w++) {
				final TokenSequence window = fileTokens.substring(startPos, startPos + config.getWindowSize(w));
				if (window != null) {
					double d = similarityStrategy.computeDistance(window);
					if (d < minDistance) {
						minDistance = d;
						minWindowSize = config.getWindowSize(w);
					}
				}
			}
			
			if (minDistance <= config.getDistanceThreshold()) {
				return new Fragment(filepath, fileTokens, startPos, startPos+minWindowSize, minDistance); 
			} else {
				return null;
			}
		}
	}
	

}
