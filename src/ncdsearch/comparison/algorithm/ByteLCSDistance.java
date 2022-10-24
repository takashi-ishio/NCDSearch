package ncdsearch.comparison.algorithm;


import ncdsearch.comparison.ICodeDistanceStrategy;
import ncdsearch.comparison.TokenSequence;

/**
 * Byte-level, normalized distance based on longest common subsequence. 
 * Its token version has been used by Yoshimura et al. IWSC 2012 and Kanda et al. SPLC 2013 papers.
 */
public class ByteLCSDistance implements ICodeDistanceStrategy {

	private byte[] query;
	
	public ByteLCSDistance(TokenSequence query) {
		this.query = query.toByteArray();
	}

	@Override
	public double computeDistance(TokenSequence code) {
		byte[] another = code.toByteArray();
		int lcs = computeLCS(query, another);
	    return 1 - lcs * 1.0 / (query.length + another.length - lcs);
	}

	static int computeLCS(byte[] query, byte[] another) {
		int[][] score = new int[query.length][another.length];
		int maxScore = 0;
	    for (int i=0; i<query.length; i++) {
	    	for (int j=0; j<another.length; j++) {
	    		if (query[i] == another[j]) {
		    		score[i][j] = (i==0 || j==0) ? 1: score[i-1][j-1] + 1;
	    		} else {
	    			int s1 = (i>0) ? score[i-1][j] : 0;
	    			int s2 = (j>0) ? score[i][j-1] : 0;
	    			score[i][j] = Math.max(s1, s2);
	    		}
	    		maxScore = Math.max(maxScore, score[i][j]);
	    	}
	    }
	    return maxScore;
	}
	
	@Override
	public void close() {
		// This object has no system resource. 
	}
}
