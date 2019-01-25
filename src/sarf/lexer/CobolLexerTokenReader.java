package sarf.lexer;


import org.antlr.v4.runtime.Lexer;

import sarf.lexer.lang.CobolLexer;


/**
 * An implementation of TokenReader that wraps an ANTLR lexer.
 */
public class CobolLexerTokenReader extends LexerTokenReader {

	/**
	 * 
	 * @param filetype
	 * @param lexer
	 */
	public CobolLexerTokenReader(FileType filetype, Lexer lexer) {
		super(filetype, lexer);
	}
	
	@Override
	public boolean next() {
		token = lexer.nextToken();
		while (token.getType() != Lexer.EOF && 
			   token.getChannel() == CobolLexer.HIDDEN) {
			token = lexer.nextToken();
		}
		return token.getType() != Lexer.EOF;
	}
	
	@Override
	public String getToken() {
		if (token.getType() == CobolLexer.DOT_FS) {
			return ".";
		} else {
			return super.getToken();
		}
	}
}
