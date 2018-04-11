package ncdsearch.experimental;

import org.junit.Assert;
import org.junit.Test;

import ncdsearch.experimental.ByteLevenshteinDistance;

public class LCSTest {

	@Test
	public void testByteLCS() {
		byte[] b1 = "ABCDEFG".getBytes();
		byte[] b2 = "ABxEFG".getBytes();
		Assert.assertEquals(5, ByteLevenshteinDistance.computeLCS(b1, b2));

		Assert.assertEquals(0, ByteLevenshteinDistance.computeLCS(new byte[0], b1));
		Assert.assertEquals(0, ByteLevenshteinDistance.computeLCS(b1, new byte[0]));
		Assert.assertEquals(5, ByteLevenshteinDistance.computeLCS(b1, "xBCDEFy".getBytes()));
	}
	
	@Test
	public void testByteLevenshtein() {
		byte[] b1 = "ABCDEFG".getBytes();
		byte[] b2 = "ABxEFG".getBytes();
		Assert.assertEquals(0, ByteLevenshteinDistance.computeLevenshteinDistance(b1, b1));
		Assert.assertEquals(2, ByteLevenshteinDistance.computeLevenshteinDistance(b1, b2));
		Assert.assertEquals(2, ByteLevenshteinDistance.computeLevenshteinDistance(b1, "xBCDEFy".getBytes()));
		Assert.assertEquals(4, ByteLevenshteinDistance.computeLevenshteinDistance(b1, "xBEFy".getBytes()));

		Assert.assertEquals(7, ByteLevenshteinDistance.computeLevenshteinDistance(new byte[0], b1));
		Assert.assertEquals(7, ByteLevenshteinDistance.computeLevenshteinDistance(b1, new byte[0]));
	}

}
