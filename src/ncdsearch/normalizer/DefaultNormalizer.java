package ncdsearch.normalizer;

import sarf.lexer.FileType;
import sarf.lexer.TokenReader;

public class DefaultNormalizer implements TokenReader {

	private TokenReader reader;
	
	public DefaultNormalizer(TokenReader r) {
		this.reader = r;
	}
	
	@Override
	public boolean next() {
		return reader.next();
	}
	
	@Override
	public String getToken() {
		return reader.getToken();
	}
	
	@Override
	public int getCharPositionInLine() {
		return reader.getCharPositionInLine();
	}
	
	@Override
	public int getLine() {
		return reader.getLine();
	}
	
	@Override
	public int getTokenType() {
		return reader.getTokenType();
	}
	
	@Override
	public FileType getFileType() {
		return reader.getFileType();
	}
}
