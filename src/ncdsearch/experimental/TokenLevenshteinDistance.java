package ncdsearch.experimental;

import ncdsearch.ICodeDistanceStrategy;
import ncdsearch.TokenSequence;

public class TokenLevenshteinDistance implements ICodeDistanceStrategy {

	private TokenSequence query;
	
	public TokenLevenshteinDistance(TokenSequence query) {
		this.query = query;
	}
	
	protected int[][] computeDistanceTable(TokenSequence code) {
		int[][] distance = new int[query.size()+1][code.size()+1];
	    for (int i=0; i<query.size()+1; i++) {
	    	distance[i][0] = i;
	    }
	    for (int j=0; j<code.size()+1; j++) {
	    	distance[0][j] = j;
	    }
	    	
	    for (int i=1; i<query.size()+1; i++) {
		    String t1 = query.getToken(i-1);
	    	for (int j=1; j<code.size()+1; j++) {
	    		String t2 = code.getToken(j-1);

	    		int edit = t1.equals(t2) ? 0 : 1;
	    		int d1 = Math.min(distance[i-1][j]+1, distance[i][j-1]+1);
    			int d2 = Math.min(d1, distance[i-1][j-1]+edit);
    			distance[i][j] = d2;
	    	}
	    }
		return distance;
	}
	
	@Override
	public double computeDistance(TokenSequence code) {
		int[][] distance = computeDistanceTable(code);
		return distance[query.size()][code.size()];
	}
	
	@Override
	public void close() {
		// This object has no system resource. 
	}
}
