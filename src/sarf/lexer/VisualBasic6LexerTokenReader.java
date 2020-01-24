package sarf.lexer;


import org.antlr.v4.runtime.Lexer;

import sarf.lexer.lang.CobolLexer;
import sarf.lexer.lang.VisualBasic6Lexer;


/**
 * An implementation of TokenReader that wraps an ANTLR lexer.
 * We ignore whitespace, newline tokens and comments in VisualBasic6 files.
 */
public class VisualBasic6LexerTokenReader extends LexerTokenReader {

	/**
	 * 
	 * @param filetype
	 * @param lexer
	 */
	public VisualBasic6LexerTokenReader(FileType filetype, Lexer lexer) {
		super(filetype, lexer);
	}
	
	@Override
	public boolean next() {
		token = lexer.nextToken();
		while (token.getType() != Lexer.EOF && 
			   (token.getType() == VisualBasic6Lexer.WS ||
			    token.getType() == VisualBasic6Lexer.NEWLINE ||
				token.getChannel() == VisualBasic6Lexer.HIDDEN)) {
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
