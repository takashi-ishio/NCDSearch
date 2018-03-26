package ncdsearch.experimental;

import org.junit.Assert;
import org.junit.Test;

import ncdsearch.experimental.ByteLCSDistance;

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
}
