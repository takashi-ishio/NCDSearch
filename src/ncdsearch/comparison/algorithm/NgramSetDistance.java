package ncdsearch.comparison.algorithm;

import java.util.Arrays;

import gnu.trove.set.hash.TLongHashSet;
import ncdsearch.comparison.ICodeDistanceStrategy;
import ncdsearch.comparison.TokenSequence;

/**
 * This strategy uses Jaccard index of n-gram sets (1 <= n <= 7).
 * NgramDistance uses a multiset, while this class uses a set.
 */
public class NgramSetDistance implements ICodeDistanceStrategy {

	private int n;
	private long[] query;
	
	public NgramSetDistance(TokenSequence query, int n) {
		this.query = createNgramSet(query.toByteArray(), n);
		this.n = n;
	}
	
	static long[] createNgramSet(byte[] buf, int n) {
		assert n < 8;
		TLongHashSet result = new TLongHashSet(buf.length-(n-1));
		for (int i=0; i<buf.length-(n-1); i++) {
			long l = 0;
			for (int j=0; j<n; j++) {
				l = (l << 8) + buf[i + j]; 
			}
			result.add(l);
		}
		long[] array = result.toArray();
		Arrays.sort(array);
		return array;
	}

	@Override
	public double computeDistance(TokenSequence code) {
		long[] another = createNgramSet(code.toByteArray(), n);
		return 1 - computeSimilarity(query, another);
	}

	static double computeSimilarity(long[] query, long[] another) {
		int intersection = computeIntersection(query, another);
		return intersection * 1.0 / (query.length + another.length - intersection);
	}

	static int computeIntersection(long[] query, long[] another) {
		int count = 0;
		int i = 0;
		int j = 0;
		while (i < query.length && j < another.length) {
			if (query[i] == another[j]) {
				count++;
				i++;
				j++;
			} else if (query[i] < another[j]) {
				i++;
			} else {
				assert query[i] > another[j];
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
