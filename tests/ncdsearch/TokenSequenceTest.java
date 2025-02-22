package ncdsearch;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import ncdsearch.comparison.TokenSequence;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;

public class TokenSequenceTest {

	public static final String JAVA_CODE_EXAMPLE = "class C { \n int x = 0; \n int y = \t 1; \n}\n";
	public static final String JAVA_CODE_TOKENS =  "class C { int x = 0 ; int y = 1 ; } ";
	@Test
	public void testSubstring() {
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, JAVA_CODE_EXAMPLE.getBytes());
		TokenSequence sequence = new TokenSequence(reader, false);
		Assert.assertEquals(14, sequence.size());
		Assert.assertEquals("class", sequence.getToken(0));
		Assert.assertEquals(1, sequence.getLine(0));
		Assert.assertEquals(1, sequence.getCharPositionInLine(0));
		Assert.assertEquals(6, sequence.getEndCharPositionInLine(0));
		Assert.assertEquals("C", sequence.getToken(1));
		Assert.assertEquals(1, sequence.getLine(1));
		Assert.assertEquals(7, sequence.getCharPositionInLine(1));
		Assert.assertEquals(8, sequence.getEndCharPositionInLine(1));
		Assert.assertEquals("}", sequence.getToken(13));
		Assert.assertEquals(1, sequence.getCharPositionInLine(13));
		Assert.assertEquals(2, sequence.getEndCharPositionInLine(13));
		
		TokenSequence sub = sequence.substring(1, 4);
		Assert.assertEquals(3, sub.size());
		Assert.assertEquals("C", sub.getToken(0));
		Assert.assertEquals("{", sub.getToken(1));
		Assert.assertEquals("int", sub.getToken(2));
		Assert.assertEquals(1, sub.getLine(0));
		Assert.assertEquals(2, sub.getCharPositionInLine(2));
		Assert.assertEquals(5, sub.getEndCharPositionInLine(2));

		TokenSequence line = sequence.substringByLine(2, 2);
		Assert.assertEquals(5, line.size());
		Assert.assertEquals("int", line.getToken(0));
		Assert.assertEquals("x", line.getToken(1));
		Assert.assertEquals("=", line.getToken(2));
		Assert.assertEquals("0", line.getToken(3));
		Assert.assertEquals(";", line.getToken(4));
		Assert.assertEquals(2, line.getLine(0));

		Assert.assertArrayEquals(("class\0C\0{\0int\0x\0=\0" + "0\0;\0int\0y\0=\0" + "1\0;\0}\0").getBytes(), sequence.toByteArray());
		Assert.assertArrayEquals("C\0{\0int\0".getBytes(), sub.toByteArray());
		Assert.assertArrayEquals(("int\0x\0=\0" + "0\0;\0").getBytes(), line.toByteArray());
	}

	@Test
	public void testEncoding() {
		Charset charset = StandardCharsets.UTF_16;
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, JAVA_CODE_EXAMPLE.getBytes(charset), charset);
		TokenSequence sequence = new TokenSequence(reader, false);
		Assert.assertEquals(14, sequence.size());

		TokenReader reader2 = TokenReaderFactory.create(FileType.JAVA, JAVA_CODE_EXAMPLE.getBytes(StandardCharsets.UTF_16BE), charset);
		TokenSequence sequence2 = new TokenSequence(reader2, false);
		Assert.assertEquals(14, sequence2.size());

		try {
			ByteArrayOutputStream withBOM = new ByteArrayOutputStream();
			withBOM.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
			withBOM.write(JAVA_CODE_EXAMPLE.getBytes());
			TokenReader reader3 = TokenReaderFactory.create(FileType.JAVA, withBOM.toByteArray(), StandardCharsets.UTF_8);
			TokenSequence sequence3 = new TokenSequence(reader3, false);
			Assert.assertEquals(14, sequence3.size());
		} catch (IOException e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testBytePositions() {
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, JAVA_CODE_EXAMPLE.getBytes());
		// Tokens: "class_C_{_int_x_=_0_;_int_y_=_1_;_}_"
		int[] expected = {0, 6, 8, 10, 14, 16, 18, 20, 22, 26, 28, 30, 32, 34, 36};
		TokenSequence sequence = new TokenSequence(reader, false);
		Assert.assertArrayEquals(expected, sequence.getBytePositions());
		for (int i=0; i<expected.length; i++) {
			Assert.assertEquals(expected[i], sequence.getBytePosition(i));
		}
		
		int from = 1;
		int to = 3;
		TokenSequence sub = sequence.substring(from, to);
		int[] expected_sub = new int[]{6, 8, 10};
		Assert.assertArrayEquals(expected_sub, sub.getBytePositions());
		for (int i=0; i<to-from+1; i++) {
			Assert.assertEquals(expected_sub[i], sub.getBytePosition(i));
		}
		
		TokenSequence line = sequence.substringByLine(2, 3);
		// expected: "int x = 0 ; int y = 1 ; "
		int[] expected_line = new int[]{10, 14, 16, 18, 20, 22, 26, 28, 30, 32, 34};
		from = 3;
		to = 13;
		Assert.assertArrayEquals(expected_line, line.getBytePositions());
		for (int i=0; i<to-from+1; i++) {
			Assert.assertEquals(expected_line[i], line.getBytePosition(i));
		}
	}
	
	@Test
	public void testToString() {
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, JAVA_CODE_EXAMPLE.getBytes());
		TokenSequence sequence = new TokenSequence(reader, false);
		Assert.assertEquals(JAVA_CODE_TOKENS, sequence.toString());

		TokenSequence sub = sequence.substring(2, 5);
		Assert.assertEquals("{ int x ", sub.toString());
		
		TokenSequence line = sequence.substringByLine(2, 2);
		Assert.assertEquals("int x = 0 ; ", line.toString());
	}

	@Test
	public void testLineHeadPositions() {
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, JAVA_CODE_EXAMPLE.getBytes());
		TokenSequence sequence = new TokenSequence(reader, false);
		Assert.assertArrayEquals(new int[] {0, 3, 8, 13}, sequence.getLineHeadTokenPositions());

		TokenSequence sub = sequence.substring(1, 14);
		Assert.assertArrayEquals(new int[] {2, 7, 12}, sub.getLineHeadTokenPositions());

		TokenSequence lines = sequence.substringByLine(2, 3);
		Assert.assertArrayEquals(new int[] {0, 5}, lines.getLineHeadTokenPositions());
	}
	
	@Test
	public void testFullScanPositions() {
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, JAVA_CODE_EXAMPLE.getBytes());
		TokenSequence sequence = new TokenSequence(reader, false);
		Assert.assertArrayEquals(new int[] {0, 1, 2, 3, 4}, sequence.getFullPositions(10));

		TokenSequence sub = sequence.substring(1, 10);
		Assert.assertArrayEquals(new int[] {0, 1}, sub.getFullPositions(8));

		TokenSequence lines = sequence.substringByLine(2, 3);
		Assert.assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5}, lines.getFullPositions(5));
	}

	@Test
	public void testLineCount() {
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, JAVA_CODE_EXAMPLE.getBytes());
		TokenSequence sequence = new TokenSequence(reader, false);
		Assert.assertEquals(4, sequence.getLineCount());

		// Note: LineCount does not change by substring
	}

}
