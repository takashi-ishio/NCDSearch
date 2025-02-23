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
		
		try (IReport report = config.getReport(); 
			StrategyManager strategyInstances = new StrategyManager()) {
			
			Concurrent c = new Concurrent(config.getThreadCount(), null);
			
			try (IFiles files = config.getFiles()) { 

				for (IFile f = files.next(); f != null; f = files.next()) {
					String targetPath = f.getPath();
					if (config.isVerbose()) {
						System.err.println(targetPath);
					}
					
					FileType type = config.getTargetLanguage(targetPath);
					if (TokenReaderFactory.isSupported(type)) {
						
						// Analyze the content if the file is a target programming language 
						c.execute(new SearchTask(targetPath, type, f, report, strategyInstances));
					}
				}
				c.waitComplete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public class StrategyManager implements AutoCloseable {

		/** 
		 * This is a List to record all strategy objects created for multi-threaded search
		 */
		private List<ICodeDistanceStrategy> createdStrategyInstances = Collections.synchronizedList(new ArrayList<ICodeDistanceStrategy>());

		/** 
		 * A search strategy object is created for each thread
		 */
		private ThreadLocal<ICodeDistanceStrategy> instances = new ThreadLocal<ICodeDistanceStrategy>() {
			@Override
			protected ICodeDistanceStrategy initialValue() {
				ICodeDistanceStrategy similarityStrategy = config.createStrategy();
				createdStrategyInstances.add(similarityStrategy);
				return similarityStrategy;
			}
		};

		public StrategyManager() {
		}
		
		/**
		 * @return a thread-local instance of a strategy object.
		 */
		public ICodeDistanceStrategy getThreadLocalInstance() {
			return instances.get();
		}
		
		/**
		 * This method releases all strategy instances.
		 */
		public void close() {
			for (ICodeDistanceStrategy s: createdStrategyInstances) {
				s.close();
			}
		}
		
	}

	
	/**
	 * This class implements a search for a single file 
	 */
	public class SearchTask implements Concurrent.Task {
		
		private String targetPath;
		private FileType type;
		private IFile target;
		private IReport report;
		private StrategyManager strategyInstances;

		public SearchTask(String targetPath, FileType type, IFile target, IReport report, StrategyManager strategyInstances) {
			this.targetPath = targetPath;
			this.type = type;
			this.target = target;
			this.report = report;
			this.strategyInstances = strategyInstances;
		}

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
				
				// Count the number of lines and tokens processed by this search
				report.recordAnalyzedFile(targetPath, fileTokens.getLineCount(), fileTokens.size());

				// Identify a similar code fragment for each position (if exists)
				ICodeDistanceStrategy similarityStrategy = strategyInstances.getThreadLocalInstance();
				ArrayList<Fragment> fragments;
				if (similarityStrategy instanceof IVariableWindowStrategy) {
					fragments = applyVariableWindowStrategy(targetPath, fileTokens, positions, config.getLargestWindowSize(), config.getDistanceThreshold(), (IVariableWindowStrategy)similarityStrategy); 
				} else {
					fragments = applyFixedWindowStrategy(targetPath, fileTokens, positions, config.getWindowSizeList(), config.getDistanceThreshold(), similarityStrategy);
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
		
	}
	
	/**
	 * Find the best match for a particular source code location using a given strategy.
	 * @param filepath specifies the file name.
	 * @param fileTokens specifies the file content.
	 * @param startPositions specify positions of the comparison.
	 * @param windowSize specifies the maximum window size for comparison.
	 * @param distanceThreshold specifies a threshold for a report.
	 * @param strategy specifies an algorithm for comparison.
	 * @return a code fragment that matches the given query if its distance is less than or equal to a threshold
	 */
	public static ArrayList<Fragment> applyVariableWindowStrategy(String filepath, TokenSequence fileTokens, int[] startPositions, int windowSize, double distanceThreshold, IVariableWindowStrategy strategy) {
		ArrayList<Fragment> fragments = new ArrayList<>();
		for (int startPos: startPositions) {
			double distance = strategy.findBestMatch(fileTokens, startPos, startPos + windowSize, distanceThreshold);
			if (distance <= distanceThreshold) {
				int w = strategy.getBestWindowSize();
				fragments.add(new Fragment(filepath, fileTokens, startPos, startPos + w, distance)); 
			}
		}
		return fragments;
	}

	/**
	 * Find the best match for a particular position using a strategy by using multiple window size
	 * @param filepath specifies the file name.
	 * @param fileTokens specifies the file content.
	 * @param startPos specifies the position of the comparison.
	 * @param windowSize specifies the maximum window size for comparison.
	 * @param distanceThreshold specifies a threshold for a report.
	 * @param strategy specifies an algorithm for comparison.
	 * @return
	 */
	public static ArrayList<Fragment> applyFixedWindowStrategy(String filepath, TokenSequence fileTokens, int[] startPositions, int[] windowSizeList, double distanceThreshold, ICodeDistanceStrategy strategy) {
		ArrayList<Fragment> fragments = new ArrayList<>();
		for (int startPos: startPositions) {
			// Try multiple window size
			double minDistance = Double.MAX_VALUE;
			int minWindowSize = -1;
			for (int w: windowSizeList) {
				final TokenSequence window = fileTokens.substring(startPos, startPos + w);
				if (window != null) {
					double d = strategy.computeDistance(window);
					if (d < minDistance) {
						minDistance = d;
						minWindowSize = w;
					}
				}
			}
			
			// Report the best one
			if (minDistance <= distanceThreshold) {
				fragments.add(new Fragment(filepath, fileTokens, startPos, startPos + minWindowSize, minDistance));
			}
		}		
		return fragments;
	}

}
