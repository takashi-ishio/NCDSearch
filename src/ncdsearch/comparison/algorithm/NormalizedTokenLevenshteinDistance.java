package ncdsearch.comparison.algorithm;


import ncdsearch.comparison.ICodeDistanceStrategy;
import ncdsearch.comparison.TokenSequence;

public class NormalizedTokenLevenshteinDistance extends TokenLevenshteinDistance implements ICodeDistanceStrategy {

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
