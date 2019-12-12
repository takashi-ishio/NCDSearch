package sarf.lexer;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class PlainTextReader implements TokenReader {

	private LineNumberReader reader;
	private String content;
	private int firstCharPos;
	private int lineNum;
	private int index;
	
	/**
	 * @param r represents a source/text file.  This reader is automatically closed when all the lines are consumed by a client.  
	 */
	public PlainTextReader(Reader r) {
		reader = new LineNumberReader(r);
		content = "";
	}
	
	private boolean nextNonEmptyLine() throws IOException {
		for (String line=reader.readLine(); line != null; line=reader.readLine()) {
			String trimmed = line.trim();
			if (trimmed.length() > 0) {
				content = trimmed;
				firstCharPos = line.indexOf(trimmed);
				lineNum = reader.getLineNumber();
				index = 0;
				return true;
			}
		}
		// End of File
		content = null;
		firstCharPos = 0;
		index = 0;
		reader.close();
		return false;
	}
	
	@Override
	public boolean next() {
		if (content != null) {
			index++;
			if (index >= content.length()) {
				try {
					return nextNonEmptyLine();
				} catch (IOException e) {
					content = null;
					firstCharPos = 0;
					index = 0;
				}
			} else {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public FileType getFileType() {
		return FileType.PLAINTEXT;
	}
	
	@Override
	public int getCharPositionInLine() {
		return firstCharPos + index;
	}
	
	@Override
	public int getLine() {
		return lineNum;
	}
	
	@Override
	public String getToken() {
		return Character.toString(content.charAt(index));
	}
	
	@Override
	public String getNormalizedToken() {
		return getToken();
	}
	
}
