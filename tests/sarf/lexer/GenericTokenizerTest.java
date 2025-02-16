package sarf.lexer;

import java.io.StringReader;

import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.junit.Test;

import sarf.lexer.lang.GenericLexer;

public class GenericTokenizerTest {

	@Test
	public void testTokenizer() {
		String src = "int\n  main\n  ()";
		TokenReader tokenizer = new LexerTokenReader(FileType.GENERIC, new GenericLexer(CharStreams.fromString(src)));

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

	@Test
	public void testTokenizer2() {
		String src = "void func_2(a, b){\n\t  return;\n}";
		TokenReader tokenizer = new LexerTokenReader(FileType.GENERIC, new GenericLexer(CharStreams.fromString(src)));

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("void", tokenizer.getToken());
		Assert.assertEquals(1, tokenizer.getLine());
		Assert.assertEquals(1, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("func_2", tokenizer.getToken());
		Assert.assertEquals(1, tokenizer.getLine());
		Assert.assertEquals(6, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("(", tokenizer.getToken());
		Assert.assertEquals(12, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("a", tokenizer.getToken());
		Assert.assertEquals(13, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals(",", tokenizer.getToken());
		Assert.assertEquals(14, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("b", tokenizer.getToken());
		Assert.assertEquals(16, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals(")", tokenizer.getToken());
		Assert.assertEquals(17, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("{", tokenizer.getToken());
		Assert.assertEquals(1, tokenizer.getLine());
		Assert.assertEquals(18, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("return", tokenizer.getToken());
		Assert.assertEquals(2, tokenizer.getLine());
		Assert.assertEquals(4, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals(";", tokenizer.getToken());
		Assert.assertEquals(2, tokenizer.getLine());
		Assert.assertEquals(10, tokenizer.getCharPositionInLine());

		Assert.assertTrue(tokenizer.next());
		Assert.assertEquals("}", tokenizer.getToken());
		Assert.assertEquals(3, tokenizer.getLine());
		Assert.assertEquals(1, tokenizer.getCharPositionInLine());

		Assert.assertFalse(tokenizer.next());
	}
	
	@Test
	public void testRandomString() {
		String s = "12asda!\n \n2U)($!A+D&&a9\t\f\n";
		RegexBasedGenericTokenizer t1 = new RegexBasedGenericTokenizer(new StringReader(s));
		TokenReader t2 = new LexerTokenReader(FileType.GENERIC, new GenericLexer(CharStreams.fromString(s)));
		
		while (t1.next()) {
			Assert.assertTrue(t2.next());
			Assert.assertEquals(t1.getToken(), t2.getToken());
			Assert.assertEquals(t1.getLine(), t2.getLine());
			Assert.assertEquals(t1.getCharPositionInLine(), t2.getCharPositionInLine());
		}
	}
	
}
