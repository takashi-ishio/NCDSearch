package sarf.lexer;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

public class PlainTextReaderTest {

	@Test
	public void testReading() {
		PlainTextReader r = new PlainTextReader(new StringReader("1st Line  \n   2nd Line\n"));
		Assert.assertTrue(r.next());
		Assert.assertEquals("1", r.getToken());
		Assert.assertEquals(1, r.getLine());
		Assert.assertEquals(0, r.getCharPositionInLine());
		Assert.assertTrue(r.next());
		Assert.assertEquals("s", r.getToken());
		Assert.assertEquals(1, r.getLine());
		Assert.assertEquals(1, r.getCharPositionInLine());
		Assert.assertTrue(r.next());
		Assert.assertEquals("t", r.getToken());
		Assert.assertEquals(1, r.getLine());
		Assert.assertEquals(2, r.getCharPositionInLine());
		Assert.assertTrue(r.next()); // " "
		Assert.assertTrue(r.next()); // "L"
		Assert.assertTrue(r.next()); // "i"
		Assert.assertTrue(r.next()); // "n"
		Assert.assertTrue(r.next()); // "e"
		Assert.assertTrue(r.next()); 
		Assert.assertEquals("2", r.getToken());
		Assert.assertEquals(2, r.getLine());
		Assert.assertEquals(3, r.getCharPositionInLine());
	}
}
