package ncdsearch.comparison.algorithm;


import ncdsearch.comparison.ICodeDistanceStrategy;
import ncdsearch.comparison.TokenSequence;


/**
 * This class uses a normalized Levenshtein distance of two byte arrays.
 * It is the number of edit operations (add/delete/modify) on a byte array 
 * divided by the length of the longer array.
 */
public class NormalizedByteLevenshteinDistance implements ICodeDistanceStrategy {

	private byte[] query;
	
	public NormalizedByteLevenshteinDistance(TokenSequence query) {
		this.query = query.toByteArray();
	}

	@Override
	public double computeDistance(TokenSequence code) {
		byte[] another = code.toByteArray();
	    return computeNormalizedLevenshteinDistance(query, another);
	}

	/**
	 * Compute a normalized Levenshtein distance of two byte arrays. 
	 * @param query the first array.
	 * @param another the second array.
	 * @return the number of edit operations (add/delete/modify) 
	 * divided by the length of the longer array.
	 */
	static double computeNormalizedLevenshteinDistance(byte[] query, byte[] another) {
		return computeLevenshteinDistance(query, another) * 1.0 / Math.max(query.length, another.length);
	}

	/**
	 * Compute a Levenshtein distance of two byte arrays. 
	 * @param query the first array.
	 * @param another the second array.
	 * @return the number of edit operations (add/delete/modify). 
	 */
	static int computeLevenshteinDistance(byte[] query, byte[] another) { 
		int[][] score = computeLevenshteinDistanceTable(query, another);
	    return score[query.length][another.length];
	}
	
	static int[][] computeLevenshteinDistanceTable(byte[] query, byte[] another) {
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
	    return score;
	}
	
	@Override
	public void close() {
		// This object has no system resource. 
	}
}
