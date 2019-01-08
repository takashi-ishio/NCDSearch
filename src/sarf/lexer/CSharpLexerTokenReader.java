package sarf.lexer;


import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import ncdsearch.normalizer.Normalizer;
import sarf.lexer.lang.CSharpLexer;


/**
 * An implementation of TokenReader that wraps an ANTLR lexer.
 */
public class CSharpLexerTokenReader extends LexerTokenReader {

	/**
	 * 
	 * @param filetype
	 * @param lexer
	 */
	public CSharpLexerTokenReader(FileType filetype, Lexer lexer) {
		super(filetype, lexer);
	}
	
	@Override
	public boolean next() {
		token = lexer.nextToken();
		while (token.getType() != Lexer.EOF && 
				(token.getChannel() == CSharpLexer.HIDDEN || 
				 token.getChannel() == CSharpLexer.DIRECTIVE_HIDDEN)) {
			token = lexer.nextToken();
		}
		return token.getType() != Lexer.EOF;
	}
}
