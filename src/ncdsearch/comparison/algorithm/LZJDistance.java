package ncdsearch.comparison.algorithm;

import it.unimi.dsi.fastutil.ints.IntHash;
import it.unimi.dsi.fastutil.ints.IntOpenCustomHashSet;
import ncdsearch.comparison.IVariableWindowStrategy;
import ncdsearch.comparison.TokenSequence;

/**
 * This is a simplified implementation of LZJDistance2022. 
 * This class just compare only hash of phrases without 
 * checking the actual content.
 */
public class LZJDistance implements IVariableWindowStrategy {
	
	/**
	 * This strategy assumes that each integer is a hash value.
	 * In other words, it directly uses integers as hash code.
	 */
	public static class MurmurHashStrategy implements IntHash.Strategy {
		
		@Override
		public int hashCode(int e) {
			return e;
		}
		
		@Override
		public boolean equals(int a, int b) {
			return a == b;
		}
	}
	
	private MurmurHashStrategy hashingStrategy = new MurmurHashStrategy();
	
	/**
	 * A LZSet of a query code fragment.
	 */
	private IntOpenCustomHashSet querySet;
	
	/**
	 * 
	 */
	private int bestWindowSize;
	
	/**
	 * Create a strategy instance with a query code fragment. 
	 * @param query is a query code fragment.
	 * @param strict If true, this object strictly compares LZSet.
	 */
	public LZJDistance(TokenSequence query) {
		byte[] queryBytes = query.toByteArray();
		querySet = toLZSet(queryBytes);
	}

	/**
	 * Construct a LZSet for a given byte array.
	 * @param b
	 * @return the LZSet of the byte array b.
	 */
	private IntOpenCustomHashSet toLZSet(byte[] b) {
		IntOpenCustomHashSet s = new IntOpenCustomHashSet(hashingStrategy);
		int start = 0;
		int end = 1;
		while (end <= b.length) {
			int phraseHash = MurmurHash3.murmurhash3_x86_32(b, start, end-start, 0);
			boolean modified = s.add(phraseHash);
			if (modified) {
				start = end;
			}
			end++;
		}
		return s;
	}

	/**
	 * Compare a given code fragment with the query and 
	 * find the best LZJD value and its window size 
	 * @param code is an entire file including the fragment to be compared.  
	 * @param startPos specifies the first token index of the fragment to be compared
	 * @param endPos specifies the last token index of the fragment to be compared
	 * @param threshold specifies the maximum distance of LZJD 
	 * @return the best LZJD value
	 */
	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold) {
		// Extract token positions between start--end (max window size)
		if (endPos > code.size()) endPos = code.size();

		double bestLZJD = Double.MAX_VALUE;
		bestWindowSize = 0;
		
		int firstTokenPos = code.getBytePosition(startPos);

		byte[] buf = code.toByteArray();
		
		int byteCount = code.getBytePosition(endPos) - firstTokenPos;
		IntOpenCustomHashSet s = new IntOpenCustomHashSet(2 * byteCount, hashingStrategy);

		int allowedMaxUnmatched = 1+(int)(threshold * querySet.size() * 1.0 / (1 - threshold));

		int start = 0;
		int end = 1;
		int intersection = 0;
		
		int windowSize = endPos-startPos;
		// For each token position, update LZSet and LZJD 
		for (int t=0; t<windowSize; t++) {
			
			int nextEnd = code.getBytePosition(startPos+t+1)-firstTokenPos;
			while (end <= nextEnd) {
				int phraseHash = MurmurHash3.murmurhash3_x86_32(buf, firstTokenPos + start, end-start, 0);
				boolean isNewElement = s.add(phraseHash);
				if (isNewElement) {
					start = end;
					if (querySet.contains(phraseHash)) {
						intersection++;
					}
				}
				end++;
			}
			
			int unionSize = querySet.size() + s.size() - intersection;
			double lzjd = (unionSize - intersection) * 1.0 / unionSize;
			if (lzjd < bestLZJD) {
				bestLZJD = lzjd;
				bestWindowSize = t+1;
			}

			// Terminate the loop early, if no chance 
			int unmatched = s.size() - intersection;
			if (unmatched > allowedMaxUnmatched) {
				break;
			}
		}

		return bestLZJD;
	}
	
	/**
	 * @return the length (number of tokens) of the best substring
	 * identified by the last call of findBestMatch
	 */
	public int getBestWindowSize() {
		return bestWindowSize;
	}
	
	/**
	 * This method directly calculates LZJD for a target code fragment.
	 * This method is not used in the main part of the search but available 
	 * for a quick comparison of two code fragments. 
	 * @param code is a target code fragment
	 * @return the LZJD value between the query and the target code fragment
	 */
	@Override
	public double computeDistance(TokenSequence code) {
		IntOpenCustomHashSet codeSet = toLZSet(code.toByteArray());
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
	
}
