package sarf.lexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Scanner;

public class CCFinderXLexer implements TokenReader {

	private LineNumberReader reader;
	
	private int lineNumber;
	private int column;
	private int bytepos;
	private String text;
	private String normalizedText;
	
	public CCFinderXLexer(byte[] buf) {
		this(new InputStreamReader(new ByteArrayInputStream(buf)));
	}
	
	public CCFinderXLexer(Reader r) {
		reader = new LineNumberReader(r);
	}
	
	@Override
	public FileType getFileType() {
		return FileType.CCFINDERX;
	}

	@Override
	public boolean next() {
		if (reader != null) {
			try {
				String line = reader.readLine();
				// skip virtual tokens whose size is zero
				while (line != null && line.contains("\t+0\t")) {
					line = reader.readLine();
				}
				if (line != null) {
					try (Scanner sc = new Scanner(line)) {
						sc.useDelimiter("\t|\\.");
						lineNumber = sc.nextInt(16);
						column = sc.nextInt(16);
						bytepos = sc.nextInt(16); 
						
						String c = sc.next(); // skip a size
						if (!c.startsWith("+")) {
							// read end position
							sc.next();
							sc.next();
						}
						
						String tokenText = sc.next(); 
						int idx = tokenText.indexOf('|');
						// TODO ここで &quot; などを外す作業が必要
						if (idx >= 0) {
							normalizedText = tokenText.substring(0, idx);
							text = tokenText.substring(idx+1);
						} else {
							text = tokenText;
							normalizedText = text;
						}
					}					
					return true;
				} else {
					reader.close();
					reader = null;
					return false;
				}
			} catch (IOException e) {
				reader = null;
			}
		}
		return false;
	}
	
	@Override
	public String getToken() {
		return text;
	}
	
	@Override
	public String getNormalizedToken() {
		return normalizedText;
	}
	
	@Override
	public int getLine() {
		return lineNumber;
	}
	
	@Override
	public int getCharPositionInLine() {
		return column;
	}

	/**
	 * Attribute unique to CCFinderX data  
	 * @return
	 */
	public int getBytePos() {
		return bytepos;
	}
}
