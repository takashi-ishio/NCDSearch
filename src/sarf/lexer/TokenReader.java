package sarf.lexer;

/**
 * A simple token reader that provides a sequence of tokens.
 */
public interface TokenReader {

	/**
	 * Read the next token.
	 * This method must be called before reading the first token.
	 * @return true if there exists a token.
	 */
	public boolean next();
	
	/**
	 * @return the current token.  This method returns null if the reader reached EOF (next() returned false).
	 */
	public String getToken();
	
	/**
	 * @return the line of the current token.
	 */
	public int getLine();

	/**
	 * @return the position of the current token.
	 */
	public int getCharPositionInLine();
	
	/**
	 * @return the token type.  The value is dependent on the implementation.
	 */
	public int getTokenType();
	
	/**
	 * @return file type.
	 */
	public FileType getFileType();

}
