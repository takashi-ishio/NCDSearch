package sarf.lexer;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class PlainTextReader implements TokenReader {

	private LineNumberReader reader;
	private String content;
	private int firstCharPos;
	private int lineNum;
	
	/**
	 * @param r represents a source/text file.  This reader is automatically closed when all the lines are consumed by a client.  
	 */
	public PlainTextReader(Reader r) {
		reader = new LineNumberReader(r);
	}
	
	@Override
	public boolean next() {
		try {
			String line = reader.readLine();
			if (line != null) {
				content = line.trim();
				firstCharPos = line.indexOf(content);
				lineNum = reader.getLineNumber();
				return true;
			} else {
				reader.close();
				content = null;
				firstCharPos = 0;
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	@Override
	public FileType getFileType() {
		return FileType.PLAINTEXT;
	}
	
	@Override
	public int getCharPositionInLine() {
		return firstCharPos;
	}
	
	@Override
	public int getLine() {
		return lineNum;
	}
	
	@Override
	public String getToken() {
		return content;
	}
	
	@Override
	public String getNormalizedToken() {
		return getToken();
	}
	
}
