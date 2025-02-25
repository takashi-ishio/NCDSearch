package ncdsearch.comparison.algorithm;


import ncdsearch.comparison.TokenSequence;


/**
 * This class uses a normalized Levenshtein distance of two token sequences.
 * It is the number of edit operations (add/delete/modify) on tokens  
 * divided by the length of the longer sequence.
 */
public class NormalizedTokenLevenshteinDistance extends TokenLevenshteinDistance {

	private int queryLength;
	
	public NormalizedTokenLevenshteinDistance(TokenSequence query) {
		super(query);
		this.queryLength = query.size();
	}
	
	@Override
	public double computeDistance(TokenSequence code) {
		double d = super.computeDistance(code);
		return d * 1.0 / Math.max(queryLength, code.size());
	}
	
	@Override
	public void close() {
		// This object has no system resource. 
	}
}
