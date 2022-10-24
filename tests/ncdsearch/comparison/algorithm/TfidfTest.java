package ncdsearch.comparison.algorithm;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.map.hash.TObjectIntHashMap;
import ncdsearch.comparison.TokenSequence;
import ncdsearch.comparison.algorithm.TfidfCosineDistance;
import sarf.lexer.FileType;
import sarf.lexer.TokenReaderFactory;

public class TfidfTest {

	@Test
	public void testCosineDistance() {
		TObjectIntHashMap<String> map = new TObjectIntHashMap<>();
		map.put("long", 10);
		map.put("int", 10);
		map.put("x", 1);
		map.put("y", 1);
		map.put(";", 10);
		
		TokenSequence q = new TokenSequence(TokenReaderFactory.create(FileType.JAVA, "int x;".getBytes()), false);
		TokenSequence c = new TokenSequence(TokenReaderFactory.create(FileType.JAVA, "int x;".getBytes()), false);
		TokenSequence c2 = new TokenSequence(TokenReaderFactory.create(FileType.JAVA, "int y;".getBytes()), false);
		TokenSequence c3 = new TokenSequence(TokenReaderFactory.create(FileType.JAVA, "long x;".getBytes()), false);
		
		try (TfidfCosineDistance d = new TfidfCosineDistance(map, q)) {
			Assert.assertEquals(0, d.computeDistance(c), 0.0001);
			Assert.assertEquals(1-(0.02/1.02), d.computeDistance(c2), 0.005);
			Assert.assertEquals(1-(1.01/1.02), d.computeDistance(c3), 0.005);
		}
	}

}
