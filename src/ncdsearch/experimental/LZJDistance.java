package ncdsearch.experimental;

import java.util.HashSet;

import ncdsearch.ICodeDistanceStrategy;
import ncdsearch.IVariableWindowStrategy;
import ncdsearch.TokenSequence;

public class LZJDistance implements IVariableWindowStrategy {
	
	private HashSet<ByteArrayFragment> querySet;
	private boolean strict;
	private int bestWindowSize;
	
	private ICodeDistanceStrategy secondary;
	
	private class ByteArrayFragment {
		
		private byte[] buf;
		private int start;
		private int length;
		private int hash;
		
		public ByteArrayFragment(byte[] buf, int start, int length) {
			this.buf = buf;
			this.start = start;
			this.length = length;
			this.hash = MurmurHash3.murmurhash3_x86_32(buf, start, length, 0);
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof ByteArrayFragment) {
				ByteArrayFragment another = (ByteArrayFragment)obj;
				if (this.hash == another.hash) {
					if (this.length == another.length) {
						if (strict) {
							for (int i=0; i<this.length; i++) {
								if (this.buf[i+start] != another.buf[i+another.start]) {
									return false;
								}
							}
						}
						return true;
					}
				}
			}
			return false;
		}
		
		/**
		 * A string representation of the byte sub-sequence.
		 * This may return a broken sub-string for multibyte text.
		 */
		@Override
		public String toString() {
			return new String(buf, start, length);
		}
	}
	
	public static void main(String[] args) {
		for (String arg: args) {
			LZJDistance d = new LZJDistance(true);
			HashSet<ByteArrayFragment> lzset = d.toLZSet(arg.getBytes());
			System.err.println(arg);
			System.err.print("->");
			for (ByteArrayFragment element: lzset) {
				System.err.print(" `");
				System.err.print(element.toString());
				System.err.print("'");
			}
			System.err.println();
			d.close();
		}
	}

	public LZJDistance(TokenSequence query) {
		this(query, false);
	}
	
	public LZJDistance(TokenSequence query, boolean strict) {
		this.strict = strict;
		byte[] queryBytes = query.toByteArray();
		querySet = toLZSet(queryBytes);
	}

	public void setSecondaryDistance(ICodeDistanceStrategy strategy) {
		this.secondary = strategy;
	}
	
	/**
	 * For testing purpose
	 * @param strict
	 */
	private LZJDistance(boolean strict) {
		this.querySet = new HashSet<>();
		this.strict = strict;
	}
	
	private HashSet<ByteArrayFragment> toLZSet(byte[] b) {
		HashSet<ByteArrayFragment> s = new HashSet<>();
		int start = 0;
		int end = 1;
		while (end <= b.length) {
			ByteArrayFragment bs = new ByteArrayFragment(b, start, end-start);
			boolean modified = s.add(bs);
			if (modified) {
				start = end;
			}
			end++;
		}
		return s;
	}

	/**
	 * 
	 * Find the best LZJD value and its window size
	 * @param code specifies an entire file 
	 * @param startPos specifies the first token index of LZSets
	 * @param maxEndPos specifies the last token index of the search
	 * @return
	 */
	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold) {
		// Extract token positions between start--end (max window size)
		if (endPos > code.size()) endPos = code.size();

		double bestLZJD = Double.MAX_VALUE;
		bestWindowSize = 0;
		
		int firstTokenPos = code.getBytePosition(startPos);

		byte[] buf = code.toByteArray();
		
		int byteCount = code.getBytePosition(endPos) - firstTokenPos;
		HashSet<ByteArrayFragment> s = new HashSet<>(2 * byteCount);

		int allowedMaxUnmatched = 1+(int)(threshold * querySet.size() * 1.0 / (1 - threshold));

		int start = 0;
		int end = 1;
		int intersection = 0;
		
		int windowSize = endPos-startPos;
		// For each token position, update LZSet and LZJD 
		for (int t=0; t<windowSize; t++) {
			
			int nextEnd = code.getBytePosition(startPos+t+1)-firstTokenPos;
			while (end <= nextEnd) {
				ByteArrayFragment text = new ByteArrayFragment(buf, firstTokenPos + start, end - start);
				boolean modified = s.add(text);
				if (modified) {
					start = end;
					if (querySet.contains(text)) {
						intersection++;
					}
				}
				end++;
			}
			
			int unionSize = querySet.size() + s.size() - intersection;
			double lzjd = (unionSize - intersection) * 1.0 / unionSize;
			if (lzjd < bestLZJD) {
				bestLZJD = lzjd;
				bestWindowSize = t;
			}

			// Terminate the loop early, if no chance 
			int unmatched = s.size() - intersection;
			if (unmatched > allowedMaxUnmatched) {
				break;
			}
		}

		if (secondary != null && bestWindowSize > 0) {
			return secondary.computeDistance(code.substring(startPos, startPos + bestWindowSize));
		} else {
			return bestLZJD;
		}
	}
	
	public int getBestWindowSize() {
		return bestWindowSize;
	}
	
	
	@Override
	public double computeDistance(TokenSequence code) {
		HashSet<ByteArrayFragment> codeSet = toLZSet(code.toByteArray());
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
