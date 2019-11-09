package ncdsearch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ncdsearch.eval.FileComparison;
import ncdsearch.experimental.PredictionFilter;
import sarf.lexer.DirectoryScan;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;


public class SearchMain {

	public static final String ARG_TEST_COMPARE = "--test-compare";
	private SearchConfiguration config;
	
	public static void main(String[] args) {
		if (args.length > 1 && args[0].equals(ARG_TEST_COMPARE)) {
			FileComparison.main(Arrays.copyOfRange(args, 1, args.length));
			return;
		}

		SearchConfiguration config = new SearchConfiguration(args);
		SearchMain instance = new SearchMain(config);
		if (config.isValidConfiguration()) {
			if (config.isVerbose()) config.printConfig();
			instance.execute();
		} else {
			System.err.println("Could not execute a search.");
			config.printConfig();
		}
	}

	
	public SearchMain(SearchConfiguration config) {
		this.config = config;
	}
	
	
	public void execute() {
		long t = System.currentTimeMillis();
		
		
		final List<ICodeDistanceStrategy> createdStrategies = Collections.synchronizedList(new ArrayList<ICodeDistanceStrategy>());
		try {
			ThreadLocal<ICodeDistanceStrategy> strategies = new ThreadLocal<ICodeDistanceStrategy>() {
				@Override
				protected ICodeDistanceStrategy initialValue() {
					ICodeDistanceStrategy similarityStrategy = config.createStrategy();
					createdStrategies.add(similarityStrategy);
					return similarityStrategy;
				}
			};
			final Concurrent c = new Concurrent(config.getThreadCount(), System.out);
			for (String dir: config.getSourceDirs()) {
				DirectoryScan.scan(new File(dir), new DirectoryScan.Action() {
					
					@Override
					public void process(File f) {
						
						String path = f.getAbsolutePath();
						
						if (config.isSearchTarget(path)) {
							if (config.isVerbose()) System.err.println(path);

							c.execute(new Concurrent.Task() {
								@Override
								public boolean run(OutputStream out) throws IOException {

									TokenReader reader = TokenReaderFactory.create(config.getQueryLanguage(), Files.readAllBytes(f.toPath()), config.getSourceCharset());
									TokenSequence fileTokens = new TokenSequence(reader, config.useNormalization(), config.useSeparator());
							
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
											Fragment fragment = checkPosition(f, fileTokens, positions[p], similarityStrategy);
											if (fragment != null) {
												fragments.add(fragment);
											}
										}
								
										if (config.allowOverlap()) {
											// Print the raw result
											for (Fragment fragment: fragments) {
												out.write(fragment.toString().getBytes());
											}
										} else {
											// Remove redundant elements and print the result.
											ArrayList<Fragment> result = Fragment.filter(fragments);
											if (result.size() > 0) {
												for (Fragment fragment: result) {
													if (config.reportPositionDetail()) {
														out.write(fragment.toLongString().getBytes());
													} else {
														out.write(fragment.toString().getBytes());
													}
												}
											}
										}
									}
									return true;
								}
							});
						}
					}
				});
			}
			c.waitComplete();
		} finally {
			for (ICodeDistanceStrategy s: createdStrategies) {
				s.close();
			}
		}
		if (config.reportTime()) {
			long time = System.currentTimeMillis() - t;
			System.err.println("Time (ms): " + time);
		}
	}
	
	/**
	 * Compare source code of a particular position with a query 
	 * @return the best code fragment 
	 */
	private Fragment checkPosition(File f, TokenSequence fileTokens, int startPos, ICodeDistanceStrategy similarityStrategy) {
		if (similarityStrategy instanceof IVariableWindowStrategy) {
			// A single call to find the best match 
			IVariableWindowStrategy strategy = (IVariableWindowStrategy)similarityStrategy;
			int endPos = startPos + config.getLargestWindowSize();
			double distance = strategy.findBestMatch(fileTokens, startPos, endPos, config.getDistanceThreshold());
			if (distance <= config.getDistanceThreshold()) {
				int w = strategy.getBestWindowSize();
				return new Fragment(f.getAbsolutePath(), fileTokens, startPos, startPos+w, distance); 
			} else {
				return null;
			}
		}  else {
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
				return new Fragment(f.getAbsolutePath(), fileTokens, startPos, startPos+minWindowSize, minDistance); 
			} else {
				return null;
			}
		}
	}
	

}
