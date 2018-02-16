package sarf.lexer;


import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;


/**
 * An implementation of TokenReader that wraps an ANTLR lexer.
 */
public class LexerTokenReader implements TokenReader {

	private FileType filetype;
	private Lexer lexer;
	private Token token;
	
	/**
	 * 
	 * @param filetype
	 * @param lexer
	 */
	public LexerTokenReader(FileType filetype, Lexer lexer) {
		this.filetype = filetype;
		this.lexer = lexer;
	}
	
	@Override
	public boolean next() {
		token = lexer.nextToken();
		return token.getType() != Lexer.EOF;
	}
	
	@Override
	public String getToken() {
		if (token.getType() != Lexer.EOF) {
			return token.getText();
		} else {
			return null;
		}
	}

	@Override
	public int getLine() {
		return token.getLine();
	}
	
	@Override
	public int getCharPositionInLine() {
		return token.getCharPositionInLine();
	}
	
	@Override
	public int getTokenType() {
		return token.getType();
	}
	
	@Override
	public FileType getFileType() {
		return filetype;
	}
}
