package ncdsearch.experimental;

import java.util.HashSet;

import gnu.trove.list.array.TIntArrayList;
import ncdsearch.ICodeDistanceStrategy;
import ncdsearch.TokenSequence;
import ncdsearch.ncd.folca.FOLCA;

public class LZJDistance implements ICodeDistanceStrategy {
	
	private HashSet<String> querySet;
	
	public static void main(String[] args) {
		for (String arg: args) {
			HashSet<String> lzset = toLZSet(arg.getBytes());
			System.err.println(arg);
			System.err.print("->");
			for (String element: lzset) {
				System.err.print(" `");
				System.err.print(element);
				System.err.print("'");
			}
			System.err.println();

			HashSet<String> lzset2 = toFOLCASet(arg.getBytes());
			System.err.println(arg);
			System.err.print("->");
			for (String element: lzset2) {
				System.err.print(" `");
				System.err.print(element);
				System.err.print("'");
			}
			System.err.println();

		}
	}

	public LZJDistance(TokenSequence query) {
		querySet = toLZSet(query.toByteArray());
	}
	
	private static HashSet<String> toFOLCASet(byte[] b) {
		FOLCA f = new FOLCA();
		f.process(b);
		f.finish();
		return f.getStringSet();
	}
	
	private static HashSet<String> toLZSet(byte[] b) {
		HashSet<String> s = new HashSet<>();
		int start = 0;
		int end = 1;
		while (end <= b.length) {
			String bs = new String(b, start, end-start);
			boolean modified = s.add(bs);
			if (modified) {
				start = end;
			}
			end++;
		}
		return s;
	}
	
	/**
	 * Find the best LZJD value and its window size
	 * @param code specifies an entire file 
	 * @param startPos specifies the first token index of LZSets
	 * @param endPos specifies the last token index of the search
	 * @return
	 */
	public double[] findBestMatch(TokenSequence code, int startPos, int endPos) {
		// Extract token positions between start--end (max window size)
		if (endPos > code.size()) endPos = code.size();
		TokenSequence slidingWindow = code.substring(startPos, endPos);
		int[] positions = slidingWindow.getBytePositions();
		assert positions.length == slidingWindow.size()+1;

		byte[] buf = code.toByteArray();
		HashSet<String> s = new HashSet<>();
		double bestLZJD = Double.MAX_VALUE;
		int bestWindow = 0;
		int intersection = 0;
		int start = 0;
		int end = 1;
		// For each token position, update LZSet and LZJD 
		for (int t=0; t<slidingWindow.size(); t++) {			
			while (end <= positions[t+1]-positions[0]) {
				String bs = new String(buf, positions[0] + start, end - start);
				boolean modified = s.add(bs);
				if (modified) {
					start = end;
					if (querySet.contains(bs)) {
						intersection++;
					}
				}
				end++;
			}
			
			int unionSize = querySet.size() + s.size() - intersection;
			double lzjd = (unionSize - intersection) * 1.0 / unionSize;
			if (lzjd < bestLZJD) {
				bestLZJD = lzjd;
				bestWindow = t;
			}
		}

		return new double[] {bestLZJD, bestWindow}; 
	}
	
	
	@Override
	public double computeDistance(TokenSequence code) {
		HashSet<String> codeSet = toLZSet(code.toByteArray());
		int codeSetSize = codeSet.size();
		codeSet.retainAll(querySet);
		int intersectionSize = codeSet.size();
		int unionSize = querySet.size() + codeSetSize - intersectionSize;
		double lzjd = (unionSize - intersectionSize) * 1.0 / unionSize;
		return lzjd;
	}
	
	@Override
	public void close() {
		// No resource used
	}
	

    /**
     * Compress method
     *
     * @param infile the name of the file to compress. Automatically appends
     * a ".lz77" extension to infile name when creating the output file
     * @exception IOException if an error occurs
     */
    public static HashSet<String> toLZSet77(byte[] buf) {
    	HashSet<String> lz77strings = new HashSet<>();
    	int pos = 0;
    	
	    String currentMatch = "";
	    StringBuffer mSearchBuffer = new StringBuffer();

	    // while there are more characters - read a character
	    while (pos < buf.length) {
	    	char nextChar = (char)buf[pos++];
	    	// look in our search buffer for a match
	    	String current = currentMatch + nextChar;
	    	int index = mSearchBuffer.indexOf(current);
	    	mSearchBuffer.append(nextChar);
	    	// if match then append nextChar to currentMatch
	    	// and update index of match
	    	if (index != -1) {
	    		currentMatch += nextChar;
	    	} else {
	    		// found longest match
    			lz77strings.add(current);
	    		//System.err.println(current);
	    		if (currentMatch.length() > 0) {
//	    			lz77strings.add(currentMatch);
//		    		System.err.println(currentMatch);
	    		}
//	    		lz77strings.add("" + nextChar);
	    		currentMatch = "";
	    		// Adjust search buffer size if necessary
	    		//trimSearchBuffer();
	    	}
	    }
	    // flush any match we may have had when EOF encountered
	    if (currentMatch.length() > 0) {
	    	lz77strings.add(currentMatch);
	    }
	    return lz77strings;
    }
    
	  
}
