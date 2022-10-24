package ncdsearch.comparison.algorithm;

import ncdsearch.comparison.IVariableWindowStrategy;
import ncdsearch.comparison.TokenSequence;

public class VariableWindowNormalizedTokenLevenshteinDistance extends TokenLevenshteinDistance implements IVariableWindowStrategy {

	private int queryLength;
	private int bestWindowSize;
	
	public VariableWindowNormalizedTokenLevenshteinDistance(TokenSequence query) {
		super(query);
		this.queryLength = query.size();
	}

	@Override
	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold) {
		if (endPos < code.size()) endPos = code.size();
		TokenSequence w = code.substring(startPos, endPos);
		if (w == null) return Double.MAX_VALUE;
		int[][] table = super.computeDistanceTable(w);
		int[] d = table[queryLength];
		int minDistance = Integer.MAX_VALUE;
		int minIndex = -1;
		for (int i=0; i<=w.size(); i++) {
			if (d[i] < minDistance) {
				minDistance = d[i];
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
	public void close() {
		super.close();
	}
	

}
