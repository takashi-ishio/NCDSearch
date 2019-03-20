package ncdsearch.experimental;


import ncdsearch.IVariableWindowStrategy;
import ncdsearch.TokenSequence;

public class VariableWindowNormalizedByteLevenshteinDistance implements IVariableWindowStrategy {

	private byte[] query;
	private int bestWindowSize;
	
	public VariableWindowNormalizedByteLevenshteinDistance(TokenSequence query) {
		this.query = query.toByteArray();
	}

	@Override
	public double computeDistance(TokenSequence code) {
		byte[] another = code.toByteArray();
		return NormalizedByteLevenshteinDistance.computeLevenshteinDistance(query, another) * 1.0 / Math.max(query.length, another.length);
	}
	
	@Override
	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold) {
		if (endPos > code.size()) endPos = code.size();
		TokenSequence w = code.substring(startPos, endPos);
		if (w == null) return Double.MAX_VALUE;
		byte[] another = w.toByteArray();
		int[][] score = NormalizedByteLevenshteinDistance.computeLevenshteinDistanceTable(query, another);
		int[] positions = w.getBytePositions();
		int bestScore = Integer.MAX_VALUE;
		for (int i=0; i<positions.length; i++) {
			int s = score[query.length][positions[i]-positions[0]];
			if (s < bestScore) {
				bestScore = s;
				bestWindowSize = i;
			}
		}
		return bestScore * 1.0 / Math.max(query.length, bestWindowSize);
	}

	@Override
	public int getBestWindowSize() {
		return bestWindowSize;
	}

	@Override
	public void close() {
		// This object has no system resource. 
	}
}
