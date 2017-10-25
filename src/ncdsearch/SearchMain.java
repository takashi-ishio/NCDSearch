package ncdsearch;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import sarf.lexer.DirectoryScan;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;


public class SearchMain {

	private static final String ARG_MIN_WINDOW = "-min";
	private static final String ARG_MAX_WINDOW = "-max";
	private static final String ARG_THRESHOLD = "-th";
	private static final String ARG_LANGUAGE = "-lang";
	
	public static void main(String[] args) {
		double WINDOW_STEP = 0.05; 
		double MIN_WINDOW = 0.8;
		double MAX_WINDOW = 1.2;
		double threshold = 0.5;
		FileType filetype = FileType.JAVA;
		ArrayList<String> sourceDirs = new ArrayList<>();
		
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
						filetype = t;
					}
				}
			} else {
				sourceDirs.add(args[idx++]);
			}
		}
		if (sourceDirs.size() == 0) sourceDirs.add(".");
		
		TokenReader reader = TokenReaderFactory.create(filetype, new InputStreamReader(System.in));
		TokenSequence queryTokens = new TokenSequence(reader); 
		
		System.err.println("Configuration: ");
		System.err.println(" Min window size ratio: " + MIN_WINDOW);
		System.err.println(" Max window size ratio: " + MAX_WINDOW);
		System.err.println(" threshold: " + threshold);
		System.err.println(" File type: " + filetype.name());
		System.err.println(" Query size: " + queryTokens.size());
		System.err.println(" Search path: " + Arrays.toString(sourceDirs.toArray()));

		TDoubleArrayList windowRatio = new TDoubleArrayList();
		for (double start = 1.0; start >= MIN_WINDOW; start -= WINDOW_STEP) {
			windowRatio.add(start);
		}
		for (double start = 1.0 + WINDOW_STEP; start <= MAX_WINDOW; start += WINDOW_STEP) {
			windowRatio.add(start);
		}
		windowRatio.sort();
		
		TIntArrayList windowSize = new TIntArrayList();
		for (int i=0; i<windowRatio.size(); i++) {
			int w = (int)Math.ceil(queryTokens.size() * windowRatio.get(i));
			if (i == 0 || windowSize.get(windowSize.size()-1) != w) {
				windowSize.add(w);
			}
		}
		final double th = threshold; // for compiler aid
		

		NormalizedCompressionDistance ncd = new NormalizedCompressionDistance(queryTokens);

		for (String dir: sourceDirs) {
			DirectoryScan.scan(new File(dir), new DirectoryScan.Action() {
				
				@Override
				public void process(File f) {
					try {
						FileType filetype = TokenReaderFactory.getFileType(f.getAbsolutePath());
						if (TokenReaderFactory.isSupported(filetype)) {
							TokenSequence fileTokens = new TokenSequence(TokenReaderFactory.create(filetype, new FileReader(f)));
							

							// Compute similarity values
							double[][] sim = new double[fileTokens.size()][windowSize.size()];
							for (int pos=0; pos<fileTokens.size(); pos++) {
								for (int w=0; w<windowSize.size(); w++) {
									TokenSequence window = fileTokens.substring(pos, pos+windowSize.get(w));
									if (window != null) {
										sim[pos][w] = ncd.ncd(window);
									} else {
										sim[pos][w] = Double.MAX_VALUE;
									}
								}
							}
							
							// Report local maximum values
							for (int pos=0; pos<fileTokens.size(); pos++) {
								for (int w=0; w<windowSize.size(); w++) {
									if (sim[pos][w] < th && isLocalMinimum(sim, pos, w)) {
										System.out.println(f.getAbsolutePath() + "," + fileTokens.getLine(pos) + "," + fileTokens.getCharPositionInLine(pos) + "," + fileTokens.getLine(pos+windowSize.get(w)-1) + "," + (fileTokens.getCharPositionInLine(pos+windowSize.get(w)-1) + fileTokens.getToken(pos+windowSize.get(w)-1).length()) + "," + sim[pos][w]);
									}
//									if (pos + w < fileTokens.size()) {
//										System.out.println(f.getAbsolutePath() + "," + fileTokens.getLine(pos) + "," + fileTokens.getLine(pos+w) + "," + sim[pos][w] + "," + pos + "," + w);
//									}
								}
							}
						}
					} catch (IOException e) {
						return;
					}
				}
			});
		}
		ncd.close();
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
		return v < value(array, p-1, w-1) && 
				v < value(array, p-1, w) &&
				v < value(array, p-1, w+1) &&
				v < value(array, p, w-1) &&
				v < value(array, p, w+1) &&
				v < value(array, p+1, w-1) &&
				v < value(array, p+1, w) &&
				v < value(array, p+1, w+1);
	}
}
