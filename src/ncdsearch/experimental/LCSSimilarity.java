package ncdsearch.experimental;

import ncdsearch.ICodeDistanceStrategy;
import ncdsearch.TokenSequence;

public class LCSSimilarity implements ICodeDistanceStrategy {

	private TokenSequence query;
	
	public LCSSimilarity(TokenSequence query) {
		this.query = query;
	}
	
	@Override
	public double computeDistance(TokenSequence code) {
		int[][] score = new int[query.size()][code.size()];
		int maxScore = 0;
	    for (int i=0; i<query.size(); i++) {
    		String t1 = query.getToken(i);
	    	for (int j=0; j<code.size(); j++) {
	    		String t2 = code.getToken(j);
	    		if (t1.equals(t2)) {
		    		score[i][j] = (i==0 || j==0) ? 1: score[i-1][j-1] + 1;
	    		} else {
	    			int s1 = (i>0) ? score[i-1][j] : 0;
	    			int s2 = (j>0) ? score[i][j-1] : 0;
	    			score[i][j] = Math.max(s1, s2);
	    		}
	    		maxScore = Math.max(maxScore, score[i][j]);
	    	}
	    }
		return code.size() + query.size() - maxScore * 2;
	}
	
	@Override
	public void close() {
		// This object has no system resource. 
	}
}
