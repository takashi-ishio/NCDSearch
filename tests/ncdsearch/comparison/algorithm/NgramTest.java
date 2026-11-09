package ncdsearch.comparison.algorithm;

import org.junit.Assert;
import org.junit.Test;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public class NgramTest {

	@Test
	public void testSameNgram() {
		byte[] b1 = "AABAACAA".getBytes();
		byte[] b2 = "AACAABAA".getBytes();

		LongArrayList l21 = NgramDistance.createNgram(b1, 2);
		LongArrayList l22 = NgramDistance.createNgram(b2, 2);
		Assert.assertEquals(7, NgramDistance.computeIntersection(l21, l22));
		Assert.assertEquals(1.0, NgramDistance.computeSimilarity(l21, l22), 0.001);

		LongArrayList l31 = NgramDistance.createNgram(b1, 3);
		LongArrayList l32 = NgramDistance.createNgram(b2, 3);
		Assert.assertEquals(6, NgramDistance.computeIntersection(l31, l32));
		Assert.assertEquals(1.0, NgramDistance.computeSimilarity(l31, l32), 0.001);
	}

	@Test
	public void testNgramSimilarity() {
		byte[] b1 = "ABCDEFG".getBytes();
		byte[] b2 = "ABxEFG".getBytes();
		
		LongArrayList l1 = NgramDistance.createNgram(b1, 1);
		LongArrayList l2 = NgramDistance.createNgram(b2, 1);
		Assert.assertEquals(5, NgramDistance.computeIntersection(l1, l2));
		Assert.assertEquals(0.625, NgramDistance.computeSimilarity(l1, l2), 0.001);

		LongArrayList l21 = NgramDistance.createNgram(b1, 2);
		LongArrayList l22 = NgramDistance.createNgram(b2, 2);
		Assert.assertEquals(3, NgramDistance.computeIntersection(l21, l22));
		Assert.assertEquals(0.375, NgramDistance.computeSimilarity(l21, l22), 0.001);
		
		LongArrayList l31 = NgramDistance.createNgram(b1, 3);
		LongArrayList l32 = NgramDistance.createNgram(b2, 3);
		Assert.assertEquals(1, NgramDistance.computeIntersection(l31, l32));
		Assert.assertEquals(0.125, NgramDistance.computeSimilarity(l31, l32), 0.001);

		LongArrayList l61 = NgramDistance.createNgram(b1, 6);
		LongArrayList l62 = NgramDistance.createNgram(b2, 6);
		Assert.assertEquals(1, l62.size());
		Assert.assertEquals(0, NgramDistance.computeIntersection(l61, l62));
		Assert.assertEquals(0, NgramDistance.computeSimilarity(l61, l62), 0.001);

		LongArrayList l71 = NgramDistance.createNgram(b1, 7);
		LongArrayList l72 = NgramDistance.createNgram(b2, 7);
		Assert.assertEquals(0, l72.size());
		Assert.assertEquals(0, NgramDistance.computeIntersection(l71, l72));
		Assert.assertEquals(0, NgramDistance.computeSimilarity(l71, l72), 0.001);
	}
	
}
