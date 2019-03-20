package ncdsearch.experimental;


import ncdsearch.IFastDistanceStrategy;
import ncdsearch.TokenSequence;

public class NormalizedTokenLevenshteinDistance extends TokenLevenshteinDistance implements IFastDistanceStrategy {

	private int queryLength;
	
	private int bestWindowSize;
	
	public NormalizedTokenLevenshteinDistance(TokenSequence query) {
		super(query);
		this.queryLength = query.size();
	}
	
	@Override
	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold) {
		if (endPos < code.size()) endPos = code.size();
		TokenSequence w = code.substring(startPos, endPos);
		if (w == null) return Double.MAX_VALUE;
		int[][] table = super.computeDistanceTable(w);
		int minDistance = Integer.MAX_VALUE;
		int minIndex = -1;
		for (int i=0; i<=w.size(); i++) {
			int d = table[queryLength][i];
			if (d < minDistance) {
				minDistance = d;
				minIndex = i;
			}
		}
		
		bestWindowSize = minIndex;
		double distance = minDistance * 1.0 / Math.max(queryLength, minIndex);
		return distance;
	}
	
	@Override
	public int getBestWindowSize() {
		return bestWindowSize;
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
