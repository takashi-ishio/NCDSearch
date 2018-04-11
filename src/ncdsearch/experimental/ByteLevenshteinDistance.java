package ncdsearch.experimental;


import ncdsearch.ICodeDistanceStrategy;
import ncdsearch.TokenSequence;

public class ByteLevenshteinDistance implements ICodeDistanceStrategy {

	private byte[] query;
	
	public ByteLevenshteinDistance(TokenSequence query) {
		this.query = query.toByteArray();
	}

	/**
	 * A file similarity used by Yoshimura [IWSC2012] and Kanda [SPLC2013]
	 */
	@Override
	public double computeDistance(TokenSequence code) {
		byte[] another = code.toByteArray();
//		int lcs = computeLCS(query, another);
//	    double d = 1 - lcs * 1.0 / (query.length + another.length - lcs);
		return computeLevenshteinDistance(query, another) * 1.0 / Math.max(query.length, another.length);
	}

	static int computeLevenshteinDistance(byte[] query, byte[] another) {
		int[][] score = new int[query.length+1][another.length+1];
		for (int i=0; i<query.length+1; i++) {
			score[i][0] = i;
		}
		for (int j=0; j<another.length+1; j++) {
			score[0][j] = j;
		}
	    for (int i=1; i<query.length+1; i++) {
	    	for (int j=1; j<another.length+1; j++) {
	    		int edit = (query[i-1] == another[j-1]) ? 0 : 1; 
	    		int s1 = Math.min(score[i-1][j]+1, score[i][j-1]+1);
	    		int s2 = Math.min(s1, score[i-1][j-1]+edit);
	    		score[i][j] = s2;
	    	}
	    }
	    return score[query.length][another.length];
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
