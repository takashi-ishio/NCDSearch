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

	@Test
	public void testSubstring() {
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, "class C { \n int x = 0; \n int y = \t 1; \n}\n".getBytes());
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
		
		Assert.assertArrayEquals(("class\0C\0{\0int\0x\0=\0" + "0\0;\0int\0y\0=\0" + "1\0;\0}\0").getBytes(), sequence.toByteArray());
		Assert.assertArrayEquals("C\0{\0int\0".getBytes(), sub.toByteArray());
	}

	@Test
	public void testEncoding() {
		Charset charset = StandardCharsets.UTF_16;
		TokenReader reader = TokenReaderFactory.create(FileType.JAVA, "class C { \n int x = 0; \n int y = \t 1; \n}\n".getBytes(charset), charset);
		TokenSequence sequence = new TokenSequence(reader, false);
		Assert.assertEquals(14, sequence.size());

		TokenReader reader2 = TokenReaderFactory.create(FileType.JAVA, "class C { \n int x = 0; \n int y = \t 1; \n}\n".getBytes(StandardCharsets.UTF_16BE), charset);
		TokenSequence sequence2 = new TokenSequence(reader2, false);
		Assert.assertEquals(14, sequence2.size());

		try {
			ByteArrayOutputStream withBOM = new ByteArrayOutputStream();
			withBOM.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
			withBOM.write("class C { \n int x = 0; \n int y = \t 1; \n}\n".getBytes());
			TokenReader reader3 = TokenReaderFactory.create(FileType.JAVA, withBOM.toByteArray(), StandardCharsets.UTF_8);
			TokenSequence sequence3 = new TokenSequence(reader3, false);
			Assert.assertEquals(14, sequence3.size());
		} catch (IOException e) {
			Assert.fail();
		}

	}

}
