package ncdsearch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import ncdsearch.eval.FileComparison;
import ncdsearch.ncd.Compressor;
import ncdsearch.normalizer.CPP14Normalizer;
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
	public static final String ARG_COMPRESSOR = "-c";
	public static final String ARG_FULLSCAN = "-full";
	public static final String ARG_QUERY = "-q";
	public static final String ARG_QUERY_START_LINE = "-sline";
	public static final String ARG_QUERY_END_LINE = "-eline";
	public static final String ARG_QUERY_DIRECT = "-e";
	public static final String ARG_NORMALIZE = "-normalize";
	public static final String ARG_LCS = "-lcs";

	private double WINDOW_STEP = 0.05; 
	private double MIN_WINDOW = 0.8;
	private double MAX_WINDOW = 1.2;
	private double threshold = 0.5;
	private boolean fullscan = false;
	private boolean verbose = false;

	private TokenSequence queryTokens;
	private ArrayList<String> sourceDirs = new ArrayList<>();
	private FileType queryFileType = FileType.JAVA;
	private Compressor compressor = null;
	private TIntArrayList windowSize;
	private boolean normalization = false;
	private boolean useLCS = false;

	
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

	public boolean isValidConfiguration() {
		assert compressor != null;
		
		return queryTokens != null && 
				windowSize != null && 
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
					compressor = Compressor.valueOf(args[idx++].toUpperCase());
				}
			} else if (args[idx].equals(ARG_QUERY)) {
				idx++;
				if (idx < args.length) {
					queryFilename = args[idx++];
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
			} else if (args[idx].equals(ARG_LCS)) {
				idx++;
				useLCS = true;
			} else {
				sourceDirs.add(args[idx++]);
			}
		}
		if (sourceDirs.size() == 0) sourceDirs.add(".");
		if (compressor == null) compressor = Compressor.ZIP;
		
		TokenReader reader;
		if (queryFilename != null) {
			try {
				File f = new File(queryFilename);
				reader = TokenReaderFactory.create(queryFileType, Files.readAllBytes(f.toPath())); 
			} catch (IOException e) {
				System.err.println("Error: Failed to read " + queryFilename + " as a query.");
				return;
			}
		} else if (queryArgs.size() > 0) {
			reader = TokenReaderFactory.create(queryFileType, new StringReader(concat(queryArgs)));
		} else {
			reader = TokenReaderFactory.create(queryFileType, new InputStreamReader(System.in)); 
		}
		
		if (normalization) reader = wrapNormalizer(reader, queryFileType);
		
		queryTokens = new TokenSequence(reader); 
		
		if (queryFilename != null) {
			queryTokens = queryTokens.substringByLine(queryStartLine, queryEndLine);
			if (queryTokens == null) {
				System.err.println("No tokens exist in lines " + queryStartLine + " through " + queryEndLine);
				return;
			}
		}
		 
		TDoubleArrayList windowRatio = new TDoubleArrayList();
		for (double start = 1.0; start >= MIN_WINDOW; start -= WINDOW_STEP) {
			windowRatio.add(start);
		}
		for (double start = 1.0 + WINDOW_STEP; start <= MAX_WINDOW; start += WINDOW_STEP) {
			windowRatio.add(start);
		}
		windowRatio.sort();
		
		windowSize = new TIntArrayList();
		for (int i=0; i<windowRatio.size(); i++) {
			int w = (int)Math.ceil(queryTokens.size() * windowRatio.get(i));
			if (i == 0 || windowSize.get(windowSize.size()-1) != w) {
				windowSize.add(w);
			}
		}

	}
	
	public void printConfig() {
		System.err.println("Configuration: ");
		System.err.println(" Compressor: " + compressor.name());
		System.err.println(" Min window size ratio: " + MIN_WINDOW);
		System.err.println(" Max window size ratio: " + MAX_WINDOW);
		if (windowSize != null) {
			System.err.println(" Window size: " + concat(windowSize));
		} else {
			System.err.println(" Window size: (unavailable)");
		}
		System.err.println(" threshold: " + threshold);
		System.err.println(" File type: " + queryFileType.name());
		if (queryTokens != null) {
			System.err.println(" Query size: " + queryTokens.size());
		} else {
			System.err.println(" Query size: (unavailable)");
		}
		System.err.println(" Search path: " + Arrays.toString(sourceDirs.toArray()));
	}
	
	public void execute() {
		if (verbose) printConfig();
		
		
		try (ICodeDistanceStrategy similarityStrategy = createStrategy()) {
			for (String dir: sourceDirs) {
				DirectoryScan.scan(new File(dir), new DirectoryScan.Action() {
					
					@Override
					public void process(File f) {
						try {
							ArrayList<Fragment> fragments = new ArrayList<>();
							
							FileType filetype = TokenReaderFactory.getFileType(f.getAbsolutePath());
							if (queryFileType == filetype) {
								if (verbose) System.err.println(f.getAbsolutePath());
								TokenReader reader = TokenReaderFactory.create(filetype, Files.readAllBytes(f.toPath()));
								if (normalization) reader = wrapNormalizer(reader, queryFileType);
								TokenSequence fileTokens = new TokenSequence(reader);
								
								int[] positions;
								if (fullscan) {
									positions = fileTokens.getFullPositions(queryTokens.size());
								} else {
									positions = fileTokens.getLineHeadTokenPositions();
								}
	
								// Compute similarity values
								double[][] distance = new double[positions.length][windowSize.size()];
								for (int p=0; p<positions.length; p++) {
									for (int w=0; w<windowSize.size(); w++) {
										TokenSequence window = fileTokens.substring(positions[p], positions[p]+windowSize.get(w));
										if (window != null) {
											distance[p][w] = similarityStrategy.computeDistance(window);
										} else {
											distance[p][w] = Double.MAX_VALUE;
										}
									}
								}
								
								// Report local maximum values
								for (int p=0; p<positions.length; p++) {
									for (int w=0; w<windowSize.size(); w++) {
										if (distance[p][w] <= threshold && isLocalMinimum(distance, p, w)) {
											Fragment fragment = new Fragment(f.getAbsolutePath(), positions[p], positions[p]+windowSize.get(w), distance[p][w]); 
											fragments.add(fragment);
										}
									}
								}
								
								// Remove redundant elements
								ArrayList<Fragment> result = Fragment.filter(fragments);
								for (Fragment fragment: result) {
									fragment.printString(fileTokens);
								}
							}
							
							
						} catch (IOException e) {
							return;
						}
					}
				});
			}
		}
	}

	public ICodeDistanceStrategy createStrategy() {
		if (useLCS) {
			return new LCSSimilarity(queryTokens);
		} else {
			return new NormalizedCompressionDistance(queryTokens, Compressor.createInstance(compressor));
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

	public static double value(double[][] array, int idx1, int idx2) {
		if (idx1 < 0 || array.length <= idx1) {
			return Double.MAX_VALUE;
		} else {
			double[] second = array[idx1];
			if (idx2 < 0 || second.length <= idx2) {
				return Double.MAX_VALUE;
			} else {
				return second[idx2];
			}
		}
	}

	public static boolean isLocalMinimum(double[][] array, int p, int w) {
		double v = array[p][w];
		return v <= value(array, p-1, w-1) && 
				v <= value(array, p-1, w) &&
				v <= value(array, p-1, w+1) &&
				v <= value(array, p, w-1) &&
				v <= value(array, p, w+1) &&
				v <= value(array, p+1, w-1) &&
				v <= value(array, p+1, w) &&
				v <= value(array, p+1, w+1);
	}
	
	private TokenReader wrapNormalizer(TokenReader r, FileType t) {
		switch (t) {
		case CPP:
			return new CPP14Normalizer(r);
		default:
			return r;
		}
	}
	
}
