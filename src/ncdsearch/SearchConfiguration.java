package ncdsearch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import ncdsearch.experimental.ByteLCSDistance;
import ncdsearch.experimental.LZJDistance;
import ncdsearch.experimental.NgramDistance;
import ncdsearch.experimental.NgramSetDistance;
import ncdsearch.experimental.NormalizedByteLevenshteinDistance;
import ncdsearch.experimental.NormalizedTokenLevenshteinDistance;
import ncdsearch.experimental.PredictionFilter;
import ncdsearch.experimental.TfidfCosineDistance;
import ncdsearch.experimental.TokenLevenshteinDistance;
import ncdsearch.experimental.VariableWindowNormalizedByteLevenshteinDistance;
import ncdsearch.experimental.VariableWindowNormalizedTokenLevenshteinDistance;
import ncdsearch.ncd.Compressor;
import ncdsearch.report.IReport;
import ncdsearch.report.StdoutReport;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;

public class SearchConfiguration {

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
	public static final String ARG_QUERY_FILENAME_STDIN = "-";
	public static final String ARG_NORMALIZE = "-normalize";
	public static final String ARG_POSITION_DETAIL = "-pos";
	public static final String ARG_THREADS = "-thread";
	public static final String ARG_PREDICTION_FILTER = "-prefilter";
	public static final String ARG_ENCODING = "-encoding";
	public static final String ARG_ALLOW_OVERLAP = "-allowoverlap";
	
	public static final String ARG_INCLUDE = "-i";
	public static final String ARG_NOSEPARATOR = "-nosep";
	public static final String ARG_SHOW_TIME = "-time";
	
	private static final String ALGORITHM_TOKEN_LEVENSHTEIN_DISTANCE = "tld";
	private static final String ALGORITHM_BYTE_LCS_DISTANCE = "blcs";
	private static final String ALGORITHM_NORMALIZED_BYTE_LEVENSHTEIN_DISTANCE = "nbld";
	private static final String ALGORITHM_NORMALIZED_TOKEN_LEVENSHTEIN_DISTANCE = "ntld";
	private static final String ALGORITHM_VARIABLE_WINDOW_NORMALIZED_BYTE_LEVENSHTEIN_DISTANCE = "vnbld";
	private static final String ALGORITHM_VARIABLE_WINDOW_NORMALIZED_TOKEN_LEVENSHTEIN_DISTANCE = "vntld";
	private static final String ALGORITHM_BYTE_NGRAM_MULTISET = "bngram";
	private static final String ALGORITHM_BYTE_NGRAM_SET = "setbngram";
	private static final String ALGORITHM_TFIDF = "tfidf";
	private static final String ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE = "lzjd";
	private static final String ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE_STRICT = "strict";
	private static final String ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE_WITH_NCD = "ncd";
	
	private static final String[] ALGORITHMS = {ALGORITHM_TOKEN_LEVENSHTEIN_DISTANCE,
			ALGORITHM_BYTE_LCS_DISTANCE, ALGORITHM_NORMALIZED_BYTE_LEVENSHTEIN_DISTANCE,
			ALGORITHM_NORMALIZED_TOKEN_LEVENSHTEIN_DISTANCE, 
			ALGORITHM_VARIABLE_WINDOW_NORMALIZED_BYTE_LEVENSHTEIN_DISTANCE,
			ALGORITHM_VARIABLE_WINDOW_NORMALIZED_TOKEN_LEVENSHTEIN_DISTANCE,
			ALGORITHM_BYTE_NGRAM_MULTISET,
			ALGORITHM_BYTE_NGRAM_SET, ALGORITHM_TFIDF, ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE, 
			ALGORITHM_LAMPEL_ZIV_JACCARD_DISTANCE_STRICT};
	
	


	private TokenSequence queryTokens;
	private ArrayList<String> sourceDirs = new ArrayList<>();
	private FileType queryFileType = null;
	private TIntArrayList windowSize;
	private boolean normalization = false;
	private PredictionFilter prefilter = null;
	private ArrayList<String> inclusionFilters = new ArrayList<>();
	
	private String argumentError;
	

	private double WINDOW_STEP = 0.05; 
	private double MIN_WINDOW = 0.8;
	private double MAX_WINDOW = 1.2;
	private double threshold = 0.5;
	private boolean fullscan = false;
	private boolean verbose = false;
	private boolean reportPositionDetail = false;
	private int threads = 0;
	private boolean allowOverlap = false;
	private boolean useSeparator = true;
	private boolean showTime = false;

	private String algorithm = "zip";
	private Charset charset;
	private String charsetError;
	
	private String queryFilename = null;
	private int queryStartLine = 0;
	private int queryEndLine = Integer.MAX_VALUE;
	private ArrayList<String> queryArgs = new ArrayList<>();

	public SearchConfiguration(String[] args) {
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
			} else if (args[idx].equals(ARG_INCLUDE)) {
				idx++;
				if (idx < args.length) {
					inclusionFilters.add(args[idx++]);
				}
			} else if (args[idx].equals(ARG_VERBOSE)) {
				idx++;
				verbose = true;
			} else if (args[idx].equals(ARG_SHOW_TIME)) {
				idx++;
				showTime = true;
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
			} else if (args[idx].equals(ARG_NOSEPARATOR)) {
				idx++;
				useSeparator = false;
			} else if (args[idx].equals(ARG_ALGORITHM)) {
				idx++;
				if (idx < args.length) {
					algorithm = args[idx++];
				}
			} else if (args[idx].equals(ARG_POSITION_DETAIL)) {
				idx++;
				reportPositionDetail = true;
			} else if (args[idx].equals(ARG_ALLOW_OVERLAP)) {
				idx++;
				allowOverlap = true;
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
			if (queryFilename.equals(ARG_QUERY_FILENAME_STDIN)) {
				reader = TokenReaderFactory.create(queryFileType, new InputStreamReader(System.in)); 
			} else {
				try {
					File f = new File(queryFilename);
					reader = TokenReaderFactory.create(queryFileType, Files.readAllBytes(f.toPath()), charset); 
				} catch (IOException e) {
					argumentError = "Failed to read " + queryFilename + " as a query.";
					return;
				}
			}
		} else if (queryArgs.size() > 0) {
			reader = TokenReaderFactory.create(queryFileType, new StringReader(concat(queryArgs)));
		} else {
			argumentError = "Query is unspecified. Use a part of a file (-q FILENAME -sline LINE -eline LINE) or tokens (-l LANG -e QUERY)";
			return;
		}
		
		queryTokens = new TokenSequence(reader, normalization, useSeparator); 
		
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
	
	private void printConfig(IReport report) {
		// entire validity
		report.writeConfig("Configuration", isValidConfiguration() ? "valid": "invalid - Could not execute a search"); 

		// algorithm name
		if (isValidAlgorithmName(algorithm)) {
			report.writeConfig("Strategy", algorithm);
		} else {
			report.writeConfig("Strategy", algorithm + " (invalid name)");
		}
		
		// window size
		report.writeConfig("Min-window-size-ratio", Double.toString(MIN_WINDOW));
		report.writeConfig("Max-window-size-ratio", Double.toString(MAX_WINDOW));
		if (windowSize != null) {
			report.writeConfig("Window-size", concat(windowSize));
		} else {
			report.writeConfig("Window-size", "(unavailable)");
		}
		report.writeConfig("Distance-threshold", Double.toString(threshold));
		report.writeConfig("Query-language", queryFileType.name());
		if (threads > 0) {
			report.writeConfig("Multi-threading", "Enabled (" + threads + " worker threads)");
		} else {
			report.writeConfig("Multi-threading", "Disabled");
		}
		if (queryTokens != null) {
			report.writeConfig("Query", queryTokens.toString());
			report.writeConfig("Query-size", Integer.toString(queryTokens.size()));
		} else {
			report.writeConfig("Query", "(unavailable)");
			report.writeConfig("Query-size", "(unavailable)");
		}
		report.writeConfig("Use-normalization", Boolean.toString(normalization));
		report.writeConfig("Use-separator", Boolean.toString(useSeparator));
		report.writeConfig("Allow-overlap", Boolean.toString(allowOverlap));
		
		if (charsetError != null) {
			report.writeConfig("Charset", charsetError + " (" + charset.displayName() + " is used)");
		} else {
			report.writeConfig("Charset", charset.displayName());
		}
		report.writeConfig("Source-path: ", Arrays.toString(sourceDirs.toArray()));
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
	
	public TokenSequence getQueryTokens() {
		return queryTokens;
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
		} else if (algorithm.startsWith(ALGORITHM_VARIABLE_WINDOW_NORMALIZED_BYTE_LEVENSHTEIN_DISTANCE)) {
			return new VariableWindowNormalizedByteLevenshteinDistance(queryTokens);
		} else if (algorithm.startsWith(ALGORITHM_VARIABLE_WINDOW_NORMALIZED_TOKEN_LEVENSHTEIN_DISTANCE)) {
			return new VariableWindowNormalizedTokenLevenshteinDistance(queryTokens);
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
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public boolean reportTime() {
		return showTime || verbose;
	}
	
	public double getDistanceThreshold() {
		return threshold;
	}
	
	/**
	 * @return the number of threads used for search.
	 */
	public int getThreadCount() {
		return threads;
	}
	
	/**
	 * @return a list of directory names including source files to be searched.
	 */
	public List<String> getSourceDirs() {
		return sourceDirs;
	}
	
	/**
	 * @param filepath
	 * @return true if the file content should be scanned. 
	 */
	public boolean isSearchTarget(String filepath) {
		FileType filetype = TokenReaderFactory.getFileType(filepath);
		boolean match = (queryFileType == filetype);
		if (!match) {
			// Check file name-based filters  
			for (String inclusionFilter: inclusionFilters) {
				match |= filepath.endsWith(inclusionFilter);
			}
		}
		return match;
	}
	
	public boolean allowOverlap() {
		return allowOverlap;
	}
	
	public int getLargestWindowSize() {
		return windowSize.get(windowSize.size()-1);
	}
	
	public int getWindowSizeCount() {
		return windowSize.size();
	}
	
	/**
	 * @param n should be between 0 and getWindowSizeCount()-1.
	 * @return n-th window size.
	 */
	public int getWindowSize(int n) {
		return windowSize.get(n);
	}
	
	public Charset getSourceCharset() {
		return charset;
	}
	
	public FileType getQueryLanguage() {
		return queryFileType;
	}
	
	public boolean isFullScan() {
		return fullscan;
	}
	
	public PredictionFilter getPrefilter() {
		return prefilter;
	}
	
	public boolean reportPositionDetail() {
		return reportPositionDetail;
	}
	
	public boolean useNormalization() {
		return normalization;
	}
	
	public boolean useSeparator() {
		return useSeparator;
	}
	
	public IReport getReport() {
		IReport report = new StdoutReport(this);
		printConfig(report);
		return report;
	}
	
	public String getArgumentError() {
		return argumentError;
	}

}
