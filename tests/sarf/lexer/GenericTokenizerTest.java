package sarf.lexer;

import java.io.StringReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import sarf.lexer.GenericTokenizer.Token;

public class GenericTokenizerTest {

	@Test
	public void testTokenizeLine() {
		List<GenericTokenizer.Token> tokens = GenericTokenizer.tokenizeLine("void func_2(a, b);", 5);
		Token t = tokens.get(0);
		Assert.assertEquals("void", t.value);
		Assert.assertEquals(5, t.line);
		Assert.assertEquals(1, t.position);

		t = tokens.get(1);
		Assert.assertEquals("func_2", t.value);
		Assert.assertEquals(5, t.line);
		Assert.assertEquals(6, t.position);

		t = tokens.get(2);
		Assert.assertEquals("(", t.value);
		Assert.assertEquals(5, t.line);
		Assert.assertEquals(12, t.position);

		t = tokens.get(3);
		Assert.assertEquals("a", t.value);
		Assert.assertEquals(13, t.position);

		t = tokens.get(4);
		Assert.assertEquals(",", t.value);
		Assert.assertEquals(14, t.position);

		t = tokens.get(5);
		Assert.assertEquals("b", t.value);
		Assert.assertEquals(16, t.position);

		t = tokens.get(6);
		Assert.assertEquals(")", t.value);
		Assert.assertEquals(17, t.position);

		t = tokens.get(7);
		Assert.assertEquals(";", t.value);
		Assert.assertEquals(18, t.position);
		
		Assert.assertEquals(8, tokens.size());
	}
	
	@Test
	public void testTokenizeFile() {
		StringReader reader = new StringReader("int\n  main\n  ()");
		List<Token> tokens = GenericTokenizer.tokenizeFile(reader);

		Token t = tokens.get(0);
		Assert.assertEquals("int", t.value);
		Assert.assertEquals(1, t.line);
		Assert.assertEquals(1, t.position);

		t = tokens.get(1);
		Assert.assertEquals("main", t.value);
		Assert.assertEquals(2, t.line);
		Assert.assertEquals(3, t.position);

		t = tokens.get(2);
		Assert.assertEquals("(", t.value);
		Assert.assertEquals(3, t.line);
		Assert.assertEquals(3, t.position);

		t = tokens.get(3);
		Assert.assertEquals(")", t.value);
		Assert.assertEquals(3, t.line);
		Assert.assertEquals(4, t.position);
	}

	@Test
	public void testTokenizer() {
		StringReader reader = new StringReader("int\n  main\n  ()");
		GenericTokenizer tokenizer = new GenericTokenizer(reader);

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("int", tokenizer.getToken());
		Assert.assertEquals(1, tokenizer.getLine());
		Assert.assertEquals(1, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("main", tokenizer.getToken());
		Assert.assertEquals(2, tokenizer.getLine());
		Assert.assertEquals(3, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("(", tokenizer.getToken());
		Assert.assertEquals(3, tokenizer.getLine());
		Assert.assertEquals(3, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals(")", tokenizer.getToken());
		Assert.assertEquals(3, tokenizer.getLine());
		Assert.assertEquals(4, tokenizer.getCharPositionInLine());

		Assert.assertFalse(tokenizer.next());
	}

}
