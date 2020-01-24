package sarf.lexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class DocxReader implements TokenReader {

	private ArrayList<String> paragraphs;
	private int paragraphIndex;
	private int charIndex;
	
	
	public DocxReader(InputStream stream) {
		paragraphs = new ArrayList<String>();
		try {
	        XWPFDocument document = new XWPFDocument(stream);
	        for (XWPFParagraph para : document.getParagraphs()) {
	        	String p = para.getText().trim();
	        	if (p.length() > 0) {
		            paragraphs.add(p);
	        	}
	        }
	        document.close();
	        stream.close();
	        paragraphIndex = 0;
	        charIndex = -1;
		} catch (IOException e) {
		}
	}
	
	@Override
	public boolean next() {
		if (paragraphIndex < paragraphs.size()) {
			String p = paragraphs.get(paragraphIndex);
			charIndex++;
			if (charIndex < p.length()) {
				return true;
			} else {
				paragraphIndex++;
				charIndex = 0;
				return paragraphIndex < paragraphs.size();
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
		return charIndex + 1;
	}
	
	@Override
	public int getLine() {
		return paragraphIndex + 1;
	}
	
	@Override
	public String getToken() {
		return Character.toString(paragraphs.get(paragraphIndex).charAt(charIndex));
	}
	
	@Override
	public String getNormalizedToken() {
		return getToken();
	}
	
	
}
