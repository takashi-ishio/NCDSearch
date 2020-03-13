package ncdsearch.experimental;

import java.util.HashSet;

import ncdsearch.IVariableWindowStrategy;
import ncdsearch.TokenSequence;

/**
 * Character-based calculation of LZJDistance
 */
public class CharLZJDistance implements IVariableWindowStrategy {
	
	private HashSet<String> querySet;
	private int bestWindowSize;
	
	
	public static void main(String[] args) {
		for (String arg: args) {
			HashSet<String> lzset = toLZSet(arg);
			System.err.println(arg);
			System.err.print("->");
			for (String element: lzset) {
				System.err.print(" `");
				System.err.print(element.toString());
				System.err.print("'");
			}
			System.err.println();
		}
	}

	public CharLZJDistance(TokenSequence query) {
		querySet = toLZSet(query.toString());
	}
	
	
	private static HashSet<String> toLZSet(String b) {
		HashSet<String> s = new HashSet<>();
		int start = 0;
		int end = 1;
		while (end <= b.length()) {
			String bs = b.substring(start, end);
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
		
		String buf = code.substring(startPos, endPos).toString();
		
		HashSet<String> s = new HashSet<>(2 * buf.length());

		int allowedMaxUnmatched = 1+(int)(threshold * querySet.size() * 1.0 / (1 - threshold));

		int start = 0;
		int end = 1;
		int intersection = 0;
		
		// For each character, update LZSet and LZJD 
		while (end <= buf.length()) {
			String bs = buf.substring(start, end);
			boolean modified = s.add(bs);
			if (modified) {
				start = end;
				if (querySet.contains(bs)) {
					intersection++;
				}
			}
			end++;
			
			int unionSize = querySet.size() + s.size() - intersection;
			double lzjd = (unionSize - intersection) * 1.0 / unionSize;
			if (lzjd < bestLZJD) {
				bestLZJD = lzjd;
				bestWindowSize = end-1;
			}

			// Terminate the loop early, if no chance 
			int unmatched = s.size() - intersection;
			if (unmatched > allowedMaxUnmatched) {
				break;
			}

		}

		return bestLZJD;
	}
	
	public int getBestWindowSize() {
		return bestWindowSize;
	}
	
	
	@Override
	public double computeDistance(TokenSequence code) {
		HashSet<String> codeSet = toLZSet(code.toString());
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
