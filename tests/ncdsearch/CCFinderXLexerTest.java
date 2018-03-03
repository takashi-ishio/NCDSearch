package ncdsearch;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import sarf.lexer.CCFinderXLexer;

public class CCFinderXLexerTest {

	private static final String lines = "36.1.56c	+0	(def_block\n" + 
			"36.1.56c\t+4\tr_bool\n" +
			"37.1.571\t+12\tid|describeAggregates\n" +
			"37.13.583\t+0\tc_func\n" +
			"37.13.583\t+1\t(paren\n" +
			"37.14.584\t+5\tr_const\n" +
			"37.1a.58a\t+4\tr_char\n" +
			"37.1f.58f\t+1\top_star\n" +
			"37.20.590\t+7\tid|pattern\n" +
			"37.27.597\t+1\tcomma\n" +
			"37.29.599\t+4\tr_bool\n" +
			"37.2e.59e\t+7\tid|verbose\n" +
			"37.35.5a5\t+1\t)paren\n" +
			"38.1.5a7\t+1\t(brace\n" +
			"39.2.5aa\t+f\tid|PQExpBufferData\n" +
			"39.12.5ba\t+3\tid|buf\n" +
			"39.15.5bd\t+1\tsuffix:semicolon\n" + 
			"7f.8.d1e	81.21.d99	l_string|&quot;SELECT spcname AS &bslash;&quot;%s&bslash;&quot;,&bslash;n&quot;&quot;  pg_catalog.pg_get_userbyid(spcowner) AS &bslash;&quot;%s&bslash;&quot;,&bslash;n&quot;&quot;  spclocation AS &bslash;&quot;%s&bslash;&quot;&quot;";

	@Test
	public void testLexer() {
		CCFinderXLexer lexer = new CCFinderXLexer(new StringReader(lines));
		Assert.assertTrue(lexer.next());
		Assert.assertEquals(54, lexer.getLine());
		Assert.assertEquals(1, lexer.getCharPositionInLine());
		Assert.assertEquals(1388, lexer.getBytePos());
		Assert.assertEquals("r_bool", lexer.getToken());
		Assert.assertEquals("r_bool", lexer.getNormalizedToken());
		Assert.assertTrue(lexer.next());
		Assert.assertEquals(55, lexer.getLine());
		Assert.assertEquals(1, lexer.getCharPositionInLine());
		Assert.assertEquals(1393, lexer.getBytePos());
		Assert.assertEquals("describeAggregates", lexer.getToken());
		Assert.assertEquals("id", lexer.getNormalizedToken());
	}
}
