package ncdsearch.experimental;

import ncdsearch.ICodeDistanceStrategy;
import ncdsearch.TokenSequence;

public class ByteLCSDistance implements ICodeDistanceStrategy {

	private byte[] query;
	
	public ByteLCSDistance(TokenSequence query) {
		this.query = query.toByteArray();
	}

	/**
	 * A file similarity used by Yoshimura [IWSC2012] and Kanda [SPLC2013]
	 */
	@Override
	public double computeDistance(TokenSequence code) {
		byte[] another = code.toByteArray();
		int lcs = computeLCS(query, another);
	    double d = 1 - lcs * 1.0 / (query.length + another.length - lcs);
		return d;
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
