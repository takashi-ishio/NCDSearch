package ncdsearch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import ncdsearch.ncd.Compressor;
import ncdsearch.ncd.DeflateStrategy;
import ncdsearch.ncd.ICompressionStrategy;
import ncdsearch.ncd.XzStrategy;
import ncdsearch.ncd.ZstdStrategy;
import sarf.lexer.DirectoryScan;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;


public class SearchMain {

	public static final String ARG_MIN_WINDOW = "-min";
	public static final String ARG_MAX_WINDOW = "-max";
	public static final String ARG_THRESHOLD = "-th";
	public static final String ARG_LANGUAGE = "-lang";
	public static final String ARG_VERBOSE = "-v";
	public static final String ARG_COMPRESSOR = "-c";
	public static final String ARG_QUERY = "-q";
	
	
	public static void main(String[] args) {
		double WINDOW_STEP = 0.05; 
		double MIN_WINDOW = 0.8;
		double MAX_WINDOW = 1.2;
		double threshold = 0.5;
		boolean verbose = false;
		FileType filetype = FileType.JAVA;
		ArrayList<String> sourceDirs = new ArrayList<>();
		Compressor compressor = null;
		String queryFilename = null;
		
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
			} else if (args[idx].equals(ARG_VERBOSE)) {
				idx++;
				verbose = true;
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
				reader = TokenReaderFactory.create(filetype, Files.readAllBytes(f.toPath())); 
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not read " + queryFilename + " as a query.");
				return;
			}
		} else {
			reader = TokenReaderFactory.create(filetype, new InputStreamReader(System.in)); 
		}
		
		TokenSequence queryTokens = new TokenSequence(reader); 
		
		System.err.println("Configuration: ");
		System.err.println(" Compressor: " + compressor.name());
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
		

		NormalizedCompressionDistance ncd = new NormalizedCompressionDistance(queryTokens, Compressor.createInstance(compressor));
		final FileType queryFileType = filetype;
		final boolean showProgress = verbose;

		for (String dir: sourceDirs) {
			DirectoryScan.scan(new File(dir), new DirectoryScan.Action() {
				
				@Override
				public void process(File f) {
					try {
						ArrayList<Fragment> fragments = new ArrayList<>();
						
						FileType filetype = TokenReaderFactory.getFileType(f.getAbsolutePath());
						if (queryFileType == filetype) {
							if (showProgress) System.err.println(f.getAbsolutePath());
							TokenSequence fileTokens = new TokenSequence(TokenReaderFactory.create(filetype, Files.readAllBytes(f.toPath())));
							
							int[] positions = fileTokens.getLineHeadTokenPositions();

							// Compute similarity values
							double[][] distance = new double[positions.length][windowSize.size()];
							for (int p=0; p<positions.length; p++) {
								for (int w=0; w<windowSize.size(); w++) {
									TokenSequence window = fileTokens.substring(positions[p], positions[p]+windowSize.get(w));
									if (window != null) {
										distance[p][w] = ncd.ncd(window);
									} else {
										distance[p][w] = Double.MAX_VALUE;
									}
								}
							}
							
							// Report local maximum values
							for (int p=0; p<positions.length; p++) {
								for (int w=0; w<windowSize.size(); w++) {
									if (distance[p][w] < th && isLocalMinimum(distance, p, w)) {
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
		return v <= value(array, p-1, w-1) && 
				v <= value(array, p-1, w) &&
				v <= value(array, p-1, w+1) &&
				v <= value(array, p, w-1) &&
				v <= value(array, p, w+1) &&
				v <= value(array, p+1, w-1) &&
				v <= value(array, p+1, w) &&
				v <= value(array, p+1, w+1);
	}
	
	private static class Fragment implements Comparable<Fragment> {

		private String filename;
		private int startPos;
		private int endPos;
		private double distance;
		
		/**
		 * @param filename
		 * @param startPos
		 * @param endPos exclusive.
		 * @param distance
		 */
		public Fragment(String filename, int startPos, int endPos, double distance) {
			this.filename = filename;
			this.startPos = startPos;
			this.endPos = endPos;
			this.distance = distance;
			assert this.startPos < this.endPos: "Zero-length fragment is not allowed.";
		}
		
		public void printString(TokenSequence fileTokens) {
			System.out.println(filename + "," + 
		                       fileTokens.getLine(startPos) + "," + 
					           fileTokens.getCharPositionInLine(startPos) + "," + 
		                       fileTokens.getLine(endPos-1) + "," + 
					           (fileTokens.getCharPositionInLine(endPos-1) + fileTokens.getToken(endPos-1).length()) + "," + 
		                       distance);
		}
		
		public boolean overlapWith(Fragment another) {
			return !(this.endPos <= another.startPos ||
				another.endPos <= this.startPos);
		}
		
		/**
		 * Compare two overlapping fragments and select a better one.
		 * @param another
		 * @return true if this object is better for output. 
		 * False if another is better.
		 */
		public boolean isBetterThan(Fragment another) {
			assert this.overlapWith(another);
			// Distance: Lower is better
			if (this.distance < another.distance) return true;
			else if (this.distance > another.distance) return false;
			else {
				// Longer is better
				int thislen = this.endPos - this.startPos;
				int anotherlen = another.endPos - another.startPos;
				if (thislen > anotherlen) return true;
				else if (thislen < anotherlen) return false;
				else {
					return this.startPos < another.startPos;
				}
			}
		}
		
		/**
		 * Sort fragments by their starting positions in the ascending order.
		 */
		@Override
		public int compareTo(Fragment another) {
			return this.startPos - another.startPos;
		}
		
		/**
		 * Remove redundant elements.
		 * @param fragments a list of fragments to be processed.  
		 * This collection is modified by this method.
		 * @return a filtered list of fragments.
		 */
		public static ArrayList<Fragment> filter(ArrayList<Fragment> fragments) {
			for (int i=0; i<fragments.size(); i++) {
				Fragment f1 = fragments.get(i);
				if (f1 == null) continue;
				for (int j=i+1; j<fragments.size(); j++) {
					Fragment f2 = fragments.get(j);
					if (f2 == null) continue;
					if (f1.overlapWith(f2)) {
						if (f1.isBetterThan(f2)) {
							fragments.set(j, null);
						} else {
							fragments.set(i, null);
							break;
						}
					}
				}
			}
			ArrayList<Fragment> result = new ArrayList<>();
			for (int i=0; i<fragments.size(); i++) {
				if (fragments.get(i) != null) {
					result.add(fragments.get(i));
				}
			}
			Collections.sort(result);
			return result;
		}
	}
}
