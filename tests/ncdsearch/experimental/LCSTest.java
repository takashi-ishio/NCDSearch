package ncdsearch.experimental;

import org.junit.Assert;
import org.junit.Test;


public class LCSTest {

	@Test
	public void testByteLCS() {
		byte[] b1 = "ABCDEFG".getBytes();
		byte[] b2 = "ABxEFG".getBytes();
		Assert.assertEquals(5, ByteLCSDistance.computeLCS(b1, b2));

		Assert.assertEquals(0, ByteLCSDistance.computeLCS(new byte[0], b1));
		Assert.assertEquals(0, ByteLCSDistance.computeLCS(b1, new byte[0]));
		Assert.assertEquals(5, ByteLCSDistance.computeLCS(b1, "xBCDEFy".getBytes()));
	}
	
	@Test
	public void testByteLevenshtein() {
		byte[] b1 = "ABCDEFG".getBytes();
		byte[] b2 = "ABxEFG".getBytes();
		Assert.assertEquals(0, NormalizedByteLevenshteinDistance.computeLevenshteinDistance(b1, b1));
		Assert.assertEquals(2, NormalizedByteLevenshteinDistance.computeLevenshteinDistance(b1, b2));
		Assert.assertEquals(2, NormalizedByteLevenshteinDistance.computeLevenshteinDistance(b1, "xBCDEFy".getBytes()));
		Assert.assertEquals(4, NormalizedByteLevenshteinDistance.computeLevenshteinDistance(b1, "xBEFy".getBytes()));

		Assert.assertEquals(7, NormalizedByteLevenshteinDistance.computeLevenshteinDistance(new byte[0], b1));
		Assert.assertEquals(7, NormalizedByteLevenshteinDistance.computeLevenshteinDistance(b1, new byte[0]));
	}

}
