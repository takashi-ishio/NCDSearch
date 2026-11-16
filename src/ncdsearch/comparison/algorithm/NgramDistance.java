package ncdsearch.comparison.algorithm;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import ncdsearch.comparison.ICodeDistanceStrategy;
import ncdsearch.comparison.TokenSequence;

/**
 * This strategy uses Jaccard index of n-gram multisets (1 <= n <= 7).
 */
public class NgramDistance implements ICodeDistanceStrategy {

	private int n;
	private LongArrayList query;
	
	public NgramDistance(TokenSequence query, int n) {
		this.query = createNgram(query.toByteArray(), n);
		this.n = n;
	}
	
	static LongArrayList createNgram(byte[] buf, int n) {
		assert n < 8;
		LongArrayList result = new LongArrayList(buf.length-(n-1));
		if (buf.length < n) return result;
		long bitmask = 0xff;
		long l = 0;
		for (int i=0; i<n-1; i++) {
			bitmask = (bitmask << 8) + 0xff;
			l = (l << 8) + buf[i]; 
		}
		for (int i=n-1; i<buf.length; i++) {
			l = ((l << 8) + buf[i]) & bitmask; 
			result.add(l);
		}
		LongArrays.unstableSort(result.elements(), 0, result.size());
		return result;
	}

	@Override
	public double computeDistance(TokenSequence code) {
		LongArrayList another = createNgram(code.toByteArray(), n);
		return 1 - computeSimilarity(query, another);
	}

	static double computeSimilarity(LongArrayList query, LongArrayList another) {
		int intersection = computeIntersection(query, another);
		return intersection * 1.0 / (query.size() + another.size() - intersection);
	}

	static int computeIntersection(LongArrayList query, LongArrayList another) {
		int count = 0;
		int i = 0;
		int j = 0;
		while (i < query.size() && j < another.size()) {
			if (query.getLong(i) == another.getLong(j)) {
				count++;
				i++;
				j++;
			} else if (query.getLong(i) < another.getLong(j)) {
				i++;
			} else {
				assert query.getLong(i) > another.getLong(j);
				j++;
			}
		}
		return count;
	}
	
	@Override
	public void close() {
		// This object has no system resource. 
	}
}
