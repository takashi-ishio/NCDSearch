package ncdsearch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import ncdsearch.eval.FileComparison;
import ncdsearch.experimental.ByteLCSDistance;
import ncdsearch.experimental.LZJDistance;
import ncdsearch.experimental.NormalizedByteLevenshteinDistance;
import ncdsearch.experimental.TokenLevenshteinDistance;
import ncdsearch.experimental.NgramDistance;
import ncdsearch.experimental.NgramSetDistance;
import ncdsearch.experimental.NormalizedTokenLevenshteinDistance;
import ncdsearch.experimental.PredictionFilter;
import ncdsearch.experimental.TfidfCosineDistance;
import ncdsearch.ncd.Compressor;
import sarf.lexer.DirectoryScan;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;


public class SearchMain {

	public static final String ARG_TEST_COMPARE = "--test-compare";
	public static final String ARG_MIN_WINDOW = "-min";
	public static final String ARG_MAX_WINDOW = "-max";
	public static final String ARG_THRESHOLD = "-th";
	public static final String ARG_LANGUAGE = "-lang";
	public static final String ARG_VERBOSE = "-v";
	public static final String ARG_ALGORITHM = "-a";
	public static final String ARG_COMPRESSOR = "-c";
	public static final String ARG_FULLSCAN = "-full";
	public static final String ARG_QUERY = "-q";
	public static final String ARG_QUERY_START_LINE = "-sline";
	public static final String ARG_QUERY_END_LINE = "-eline";
	public static final String ARG_QUERY_DIRECT = "-e";
	public static final String ARG_NORMALIZE = "-normalize";
	public static final String ARG_POSITION_DETAIL = "-pos";
	public static final String ARG_THREADS = "-thread";
	public static final String ARG_PREDICTION_FILTER = "-prefilter";
	public static final String ARG_ENCODING = "-encoding";
	
	private static final String ALGORITHM_TOKEN_LEVENSHTEIN_DISTANCE = "tld";
	private static final String ALGORITHM_BYTE_LCS_DISTANCE = "blcs";
	private static final String ALGORITHM_NORMALIZED_BYTE_LEVENSHTEIN_DISTANCE = "nbld";
	private static final String ALGORITHM_NORMALIZED_TOKEN_LEVENSHTEIN_DISTANCE = "ntld";
	private static final String ALGORITHM_BYTE_NGRAM_MULTISET = "bngram";
	private static final String ALGORITHM_BYTE_NGRAM_SET = "setbngram";
	private static final String ALGORITHM_TFIDF = "tfidf";
	private static final String ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE = "lzjd";
	private static final String ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE_STRICT = "strict";
	private static final String ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE_WITH_NCD = "ncd";
	
	private static final String[] ALGORITHMS = {ALGORITHM_TOKEN_LEVENSHTEIN_DISTANCE,
			ALGORITHM_BYTE_LCS_DISTANCE, ALGORITHM_NORMALIZED_BYTE_LEVENSHTEIN_DISTANCE,
			ALGORITHM_NORMALIZED_TOKEN_LEVENSHTEIN_DISTANCE, ALGORITHM_BYTE_NGRAM_MULTISET,
			ALGORITHM_BYTE_NGRAM_SET, ALGORITHM_TFIDF, ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE, 
			ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE_STRICT};
	
	

	private double WINDOW_STEP = 0.05; 
	private double MIN_WINDOW = 0.8;
	private double MAX_WINDOW = 1.2;
	private double threshold = 0.5;
	private boolean fullscan = false;
	private boolean verbose = false;
	private boolean reportPositionDetail = false;
	private int threads = 0;

	private String algorithm = "zip";

	private TokenSequence queryTokens;
	private ArrayList<String> sourceDirs = new ArrayList<>();
	private FileType queryFileType = null;
	private TIntArrayList windowSize;
	private boolean normalization = false;
	private PredictionFilter prefilter = null;
	
	private Charset charset;
	private String charsetError;

	
	public static void main(String[] args) {
		if (args.length > 1 && args[0].equals(ARG_TEST_COMPARE)) {
			FileComparison.main(Arrays.copyOfRange(args, 1, args.length));
			return;
		}

		SearchMain instance = new SearchMain(args);
		if (instance.isValidConfiguration()) {
			instance.execute();
		} else {
			System.err.println("Could not execute a search.");
			instance.printConfig();
		}
	}

	/**
	 * @return true if arguments are valid and also a query is specified.
	 */
	public boolean isValidConfiguration() {
		return queryTokens != null && 
				windowSize != null && 
				isValidAlgorithmName(algorithm) &&
				sourceDirs.size() > 0 &&
				windowSize.size() > 0;
	}
	
	
	public SearchMain(String[] args) {
		String queryFilename = null;
		int queryStartLine = 0;
		int queryEndLine = Integer.MAX_VALUE;
		ArrayList<String> queryArgs = new ArrayList<>();
		
		
		int idx = 0;
		while (idx < args.length) {
			if (args[idx].equals(ARG_MIN_WINDOW)) {
				idx++;
				if (idx < args.length) {
					try {
						MIN_WINDOW = Double.parseDouble(args[idx++]);
					} catch (NumberFormatException e) {
					}
				}
			} else if (args[idx].equals(ARG_MAX_WINDOW)) {
				idx++;
				if (idx < args.length) {
					try {
						MAX_WINDOW = Double.parseDouble(args[idx++]);
					} catch (NumberFormatException e) {
					}
				}
			} else if (args[idx].equals(ARG_THRESHOLD)) {
				idx++;
				if (idx < args.length) {
					try {
						threshold = Double.parseDouble(args[idx++]);
					} catch (NumberFormatException e) {
					}
				}
			} else if (args[idx].equals(ARG_LANGUAGE)) {
				idx++;
				if (idx < args.length) {
					FileType t = TokenReaderFactory.getFileType("." + args[idx++]);
					if (TokenReaderFactory.isSupported(t)) {
						queryFileType = t;
					}
				}
			} else if (args[idx].equals(ARG_VERBOSE)) {
				idx++;
				verbose = true;
			} else if (args[idx].equals(ARG_FULLSCAN)) {
				idx++;
				fullscan = true;
			} else if (args[idx].equals(ARG_COMPRESSOR)) {
				idx++;
				if (idx < args.length) {
					algorithm = args[idx++];
				}
			} else if (args[idx].equals(ARG_QUERY)) {
				idx++;
				if (idx < args.length) {
					queryFilename = args[idx++];
					FileType t = TokenReaderFactory.getFileType(queryFilename);
					if (TokenReaderFactory.isSupported(t) && queryFileType == null) {
						queryFileType = t;
					}
				}
			} else if (args[idx].equals(ARG_QUERY_START_LINE)) {
				idx++;
				if (idx < args.length) {
					queryStartLine = Integer.parseInt(args[idx++]);
				}
			} else if (args[idx].equals(ARG_QUERY_END_LINE)) {
				idx++;
				if (idx < args.length) {
					queryEndLine = Integer.parseInt(args[idx++]);
				}
			} else if (args[idx].equals(ARG_QUERY_DIRECT)) {
				idx++;
				while (idx < args.length) {
					queryArgs.add(args[idx++]);
				}
			} else if (args[idx].equals(ARG_NORMALIZE)) {
				idx++;
				normalization = true;
			} else if (args[idx].equals(ARG_ALGORITHM)) {
				idx++;
				if (idx < args.length) {
					algorithm = args[idx++];
				}
			} else if (args[idx].equals(ARG_POSITION_DETAIL)) {
				idx++;
				reportPositionDetail = true;
			} else if (args[idx].startsWith(ARG_THREADS)) {
				idx++;
				threads = Integer.parseInt(args[idx++]);
			} else if (args[idx].startsWith(ARG_ENCODING)) {
				idx++;
				String charsetName = args[idx++];
				try {
					charset = Charset.forName(charsetName);
				} catch (IllegalCharsetNameException e) {
					charsetError = "Unsupported charset name " + charsetName;
				} catch (Exception e) {
					charsetError = "Invalid parameter " + charsetName;
				}
			} else if (args[idx].equals(ARG_PREDICTION_FILTER)) {
				idx++;
				if (idx < args.length) {
					prefilter = new PredictionFilter(args[idx++]);
				}
			} else {
				sourceDirs.add(args[idx++]);
			}
		}
		if (sourceDirs.size() == 0) sourceDirs.add(".");
		if (queryFileType == null) queryFileType = FileType.JAVA;
		if (charset == null) charset = StandardCharsets.UTF_8;
		
		TokenReader reader;
		if (queryFilename != null) {
			try {
				File f = new File(queryFilename);
				reader = TokenReaderFactory.create(queryFileType, Files.readAllBytes(f.toPath()), charset); 
			} catch (IOException e) {
				System.err.println("Error: Failed to read " + queryFilename + " as a query.");
				return;
			}
		} else if (queryArgs.size() > 0) {
			reader = TokenReaderFactory.create(queryFileType, new StringReader(concat(queryArgs)));
		} else {
			reader = TokenReaderFactory.create(queryFileType, new InputStreamReader(System.in)); 
		}
		
		queryTokens = new TokenSequence(reader, normalization); 
		
		if (queryFilename != null) {
			queryTokens = queryTokens.substringByLine(queryStartLine, queryEndLine);
			if (queryTokens == null) {
				System.err.println("No tokens exist in lines " + queryStartLine + " through " + queryEndLine);
				return;
			}
		}
		
		if (prefilter != null) {
			prefilter.setQuery(queryTokens);
		}
		 
		TDoubleArrayList windowRatio = new TDoubleArrayList();
		for (double start = 1.0; start >= MIN_WINDOW; start -= WINDOW_STEP) {
			windowRatio.add(start);
		}
		for (double start = 1.0 + WINDOW_STEP; start <= MAX_WINDOW; start += WINDOW_STEP) {
			windowRatio.add(start);
		}
		windowRatio.sort();
		
		if (algorithm.startsWith(ALGORITHM_TOKEN_LEVENSHTEIN_DISTANCE)) {
			windowSize = new TIntArrayList();
			windowSize.add(queryTokens.size());
			for (int i=1; i<=threshold; i++) {
				windowSize.add(queryTokens.size() + i);
				windowSize.add(queryTokens.size() - i);
			}
			windowSize.sort();
		} else {
			windowSize = new TIntArrayList();
			for (int i=0; i<windowRatio.size(); i++) {
				int w = (int)Math.ceil(queryTokens.size() * windowRatio.get(i));
				if (i == 0 || windowSize.get(windowSize.size()-1) != w) {
					windowSize.add(w);
				}
			}
		}

	}
	
	public void printConfig() {
		System.err.println("Configuration: ");
		if (isValidAlgorithmName(algorithm)) {
			System.err.println(" Strategy: " + algorithm);
		} else {
			System.err.println(" Strategy: " + algorithm + " (invalid name)");
		}
		System.err.println(" Min window size ratio: " + MIN_WINDOW);
		System.err.println(" Max window size ratio: " + MAX_WINDOW);
		if (windowSize != null) {
			System.err.println(" Window size: " + concat(windowSize));
		} else {
			System.err.println(" Window size: (unavailable)");
		}
		System.err.println(" threshold: " + threshold);
		System.err.println(" File type: " + queryFileType.name());
		if (threads > 0) {
			System.err.println(" Multi-threading: Enabled (" + threads + " worker threads)");
		} else {
			System.err.println(" Multi-threading: Disabled");
		}
		if (queryTokens != null) {
			System.err.println(" Query size: " + queryTokens.size());
		} else {
			System.err.println(" Query size: (unavailable)");
		}
		if (charsetError != null) {
			System.err.println(" Charset: " + charsetError + " (" + charset.displayName() + " is used)");
		} else {
			System.err.println(" Charset: " + charset.displayName());
		}
		System.err.println(" Search path: " + Arrays.toString(sourceDirs.toArray()));
	}
	
	public void execute() {
		if (verbose) printConfig();
		long t = System.currentTimeMillis();
		
		
		final List<ICodeDistanceStrategy> createdStrategies = Collections.synchronizedList(new ArrayList<ICodeDistanceStrategy>());
		try {
			ThreadLocal<ICodeDistanceStrategy> strategies = new ThreadLocal<ICodeDistanceStrategy>() {
				@Override
				protected ICodeDistanceStrategy initialValue() {
					ICodeDistanceStrategy similarityStrategy = createStrategy();
					createdStrategies.add(similarityStrategy);
					return similarityStrategy;
				}
			};
			final Concurrent c = new Concurrent(threads, System.out);
			for (String dir: sourceDirs) {
				DirectoryScan.scan(new File(dir), new DirectoryScan.Action() {
					
					@Override
					public void process(File f) {
						
						FileType filetype = TokenReaderFactory.getFileType(f.getAbsolutePath());
						if (queryFileType == filetype) {
							if (verbose) System.err.println(f.getAbsolutePath());

							c.execute(new Concurrent.Task() {
								@Override
								public boolean run(OutputStream out) throws IOException {

									TokenReader reader = TokenReaderFactory.create(filetype, Files.readAllBytes(f.toPath()), charset);
									TokenSequence fileTokens = new TokenSequence(reader, normalization);
							
									if (prefilter == null || prefilter.shouldSearch(fileTokens)) {
										int[] positions;
										if (fullscan) {
											positions = fileTokens.getFullPositions(queryTokens.size());
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
								
										// Remove redundant elements and print the result.
										if (fragments.size() > 0) {
											ArrayList<Fragment> result = Fragment.filter(fragments);
											if (result.size() > 0) {
												for (Fragment fragment: result) {
													if (reportPositionDetail) {
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
		if (verbose) {
			long time = System.currentTimeMillis() - t;
			System.err.println("Time (ms): " + time);
		}
	}
	
	/**
	 * Compare source code of a particular position with a query 
	 * @return the best code fragment 
	 */
	private Fragment checkPosition(File f, TokenSequence fileTokens, int startPos, ICodeDistanceStrategy similarityStrategy) {
		if (similarityStrategy instanceof LZJDistance) {
			// special treatment
			LZJDistance lzjd = (LZJDistance)similarityStrategy;
			int endPos = startPos + windowSize.get(windowSize.size()-1);
			double distance = lzjd.findBestMatch(fileTokens, startPos, endPos, threshold);
			if (distance <= threshold) {
				int w = lzjd.getBestWindowSize();
				return new Fragment(f.getAbsolutePath(), fileTokens, startPos, startPos+w, distance); 
			} else {
				return null;
			}
		}  else {
			double minDistance = Double.MAX_VALUE;
			int minWindowSize = -1;
			for (int w=0; w<windowSize.size(); w++) {
				final TokenSequence window = fileTokens.substring(startPos, startPos+windowSize.get(w));
				if (window != null) {
					double d = similarityStrategy.computeDistance(window);
					if (d < minDistance) {
						minDistance = d;
						minWindowSize = windowSize.get(w);
					}
				}
			}
			
			if (minDistance <= threshold) {
				return new Fragment(f.getAbsolutePath(), fileTokens, startPos, startPos+minWindowSize, minDistance); 
			} else {
				return null;
			}
		}
	}
	
	public ICodeDistanceStrategy createStrategy() {
		if (algorithm.startsWith(ALGORITHM_TOKEN_LEVENSHTEIN_DISTANCE)) {
			return new TokenLevenshteinDistance(queryTokens);
		} else if (algorithm.startsWith(ALGORITHM_BYTE_LCS_DISTANCE)) {
			return new ByteLCSDistance(queryTokens);
		} else if (algorithm.startsWith(ALGORITHM_NORMALIZED_BYTE_LEVENSHTEIN_DISTANCE)) {
			return new NormalizedByteLevenshteinDistance(queryTokens);
		} else if (algorithm.startsWith(ALGORITHM_NORMALIZED_TOKEN_LEVENSHTEIN_DISTANCE)) {
			return new NormalizedTokenLevenshteinDistance(queryTokens);
		} else if (algorithm.startsWith(ALGORITHM_BYTE_NGRAM_MULTISET)) {
			int n = Integer.parseInt(algorithm.substring(ALGORITHM_BYTE_NGRAM_MULTISET.length()));
			return new NgramDistance(queryTokens, n);
		} else if (algorithm.startsWith(ALGORITHM_BYTE_NGRAM_SET)) {
			int n = Integer.parseInt(algorithm.substring(ALGORITHM_BYTE_NGRAM_SET.length()));
			return new NgramSetDistance(queryTokens, n);
		} else if (algorithm.startsWith(ALGORITHM_TFIDF)) {
			return new TfidfCosineDistance(sourceDirs, queryFileType, queryTokens, charset);
		} else if (algorithm.startsWith(ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE)) {
			boolean strict = algorithm.contains(ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE_STRICT);
			LZJDistance d = new LZJDistance(queryTokens, strict);
			if (algorithm.contains(ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE_WITH_NCD)) {
				d.setSecondaryDistance(new NormalizedCompressionDistance(queryTokens));
			}
			return d;
		}

		Compressor c = Compressor.ZIP;
		try {
			c = Compressor.valueOf(algorithm.toUpperCase());
		} catch (IllegalArgumentException e) {
		}
		return new NormalizedCompressionDistance(queryTokens, Compressor.createInstance(c));
	}
	
	public boolean isValidAlgorithmName(String name) {
		for (String al: ALGORITHMS) {
			if (name.startsWith(al)) return true; 
		}

		try {
			Compressor c = Compressor.valueOf(name.toUpperCase());
			return c != null;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public static String concat(ArrayList<String> queryArgs) {
		StringBuilder b = new StringBuilder();
		for (String arg: queryArgs) {
			b.append(arg);
			b.append(" ");
		}
		return b.toString();
	}

	public static String concat(TIntArrayList list) {
		StringBuilder b = new StringBuilder();
		for (int i=0; i<list.size(); i++) {
			if (i>0) b.append(", ");
			b.append(list.get(i));
		}
		return b.toString();
	}
	
	public TokenSequence getQueryTokens() {
		return queryTokens;
	}
	

}
