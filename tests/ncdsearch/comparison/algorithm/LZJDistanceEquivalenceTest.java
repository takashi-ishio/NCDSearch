package ncdsearch.comparison.algorithm;

import org.junit.Assert;
import org.junit.Test;

import ncdsearch.comparison.IVariableWindowStrategy;
import ncdsearch.comparison.TokenSequence;
import sarf.lexer.FileType;
import sarf.lexer.TokenReaderFactory;

/**
 * Check that the three LZJD implementations
 * (LZJDistance, LZJDistance2022, LZJDistanceTrie)
 * report the same similarity for simple code fragments.
 */
public class LZJDistanceEquivalenceTest {

	private static final String[] QUERIES = {
		"if (x == null) return 0;",
		"for (int i=0; i<n; i++) { sum += a[i]; }",
		"public int getValue() { return value; }",
		"x = x + 1;",
	};

	private static final String[] TARGETS = {
		"if (x == null) return 0;",
		"if (y == null) return 1;",
		"if (x != null) return x.size();",
		"for (int j=0; j<m; j++) { total += b[j]; }",
		"for (int i=0; i<n; i++) { sum += a[i]; } return sum;",
		"while (x != null) x = x.next;",
		"public int getSize() { return size; }",
		"x = x + 1; y = y + 1;",
		"abc",
		"",
	};

	private static TokenSequence tokenize(String code) {
		return new TokenSequence(TokenReaderFactory.create(FileType.JAVA, code.getBytes()), false);
	}

	private static IVariableWindowStrategy[] createStrategies(TokenSequence query) {
		return new IVariableWindowStrategy[] {
			new LZJDistance2022(query, true),
			new LZJDistance2022(query, false),
			new LZJDistance(query),
			new LZJDistanceTrie(query),
		};
	}

	private static void close(IVariableWindowStrategy[] strategies) {
		for (IVariableWindowStrategy s: strategies) {
			s.close();
		}
	}

	/**
	 * A query is identical to itself.
	 */
	@Test
	public void testIdenticalCode() {
		for (String q: QUERIES) {
			TokenSequence query = tokenize(q);
			IVariableWindowStrategy[] strategies = createStrategies(query);
			for (IVariableWindowStrategy s: strategies) {
				String label = s.getClass().getSimpleName() + ": " + q;
				Assert.assertEquals(label, 0.0, s.computeDistance(tokenize(q)), 0.0);
				Assert.assertEquals(label, 0.0, s.findBestMatch(query, 0, query.size(), 0.5), 0.0);
				Assert.assertEquals(label, query.size(), s.getBestWindowSize());
			}
			close(strategies);
		}
	}

	/**
	 * computeDistance returns the same value for all the implementations.
	 */
	@Test
	public void testComputeDistance() {
		for (String q: QUERIES) {
			IVariableWindowStrategy[] strategies = createStrategies(tokenize(q));
			for (String t: TARGETS) {
				TokenSequence target = tokenize(t);
				double expected = strategies[0].computeDistance(target);
				Assert.assertTrue(0.0 <= expected && expected <= 1.0);
				for (int i=1; i<strategies.length; i++) {
					String label = strategies[i].getClass().getSimpleName() + ": " + q + " vs. " + t;
					Assert.assertEquals(label, expected, strategies[i].computeDistance(target), 0.0);
				}
			}
			close(strategies);
		}
	}

	/**
	 * findBestMatch returns the same distance and window size
	 * for all the implementations when the window is similar to the query.
	 * LZJDistanceTrie may skip a dissimilar window, so that only
	 * the judgment (distance <= threshold) is compared in that case.
	 */
	@Test
	public void testFindBestMatch() {
		double[] thresholds = { 0.3, 0.5, 0.7, 0.9 };
		int matches = 0;
		for (String q: QUERIES) {
			TokenSequence query = tokenize(q);
			IVariableWindowStrategy[] strategies = createStrategies(query);
			int windowSize = (int)Math.ceil(query.size() * 1.2);
			for (String t: TARGETS) {
				TokenSequence target = tokenize(t);
				for (double threshold: thresholds) {
					for (int pos=0; pos<target.size(); pos++) {
						double expected = strategies[0].findBestMatch(target, pos, pos + windowSize, threshold);
						int expectedWindow = strategies[0].getBestWindowSize();
						for (int i=1; i<strategies.length; i++) {
							String label = strategies[i].getClass().getSimpleName() + ": " + q + " vs. " + t + " at " + pos + " (threshold=" + threshold + ")";
							double actual = strategies[i].findBestMatch(target, pos, pos + windowSize, threshold);
							Assert.assertEquals(label, expected <= threshold, actual <= threshold);
							if (expected <= threshold) {
								Assert.assertEquals(label, expected, actual, 0.0);
								Assert.assertEquals(label, expectedWindow, strategies[i].getBestWindowSize());
							}
						}
						if (expected <= threshold) matches++;
					}
				}
			}
			close(strategies);
		}
		Assert.assertTrue(matches > 0);
	}
}
