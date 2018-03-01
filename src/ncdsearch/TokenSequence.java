package ncdsearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;
import sarf.lexer.TokenReader;


public class TokenSequence {

	private ArrayList<String> tokens;
	private TIntArrayList lines;
	private TIntArrayList charpos;
	private int start;
	private int end;
	private byte[] bytes;
	private TIntArrayList bytepos;
	
	/**
	 * Create an object including all tokens obtained from a reader.
	 * @param r specifies a TokenReader.
	 */
	public TokenSequence(TokenReader r) {
		tokens = new ArrayList<>();
		lines = new TIntArrayList();
		charpos = new TIntArrayList();
		bytepos = new TIntArrayList();
		ByteArrayOutputStream s = new ByteArrayOutputStream(65536);
		try {
			while (r.next()) {
				tokens.add(r.getToken());
				lines.add(r.getLine());
				charpos.add(r.getCharPositionInLine());
				bytepos.add(s.size());
				s.write(r.getToken().getBytes());
				s.write(0);
			}
			bytepos.add(s.size());
		} catch (IOException e) {
			assert false: "ByteArrayOutputStream never throws IOException.";
		}
		start = 0;
		end = tokens.size();
		bytes = s.toByteArray();
	}
	
	/**
	 * Create a subsequence of tokens.
	 * @param base an original sequence.
	 * @param start index of a start token.
	 * @param end index of an end token (exclusive).
	 */
	private TokenSequence(TokenSequence base, int start, int end) { 
		this.tokens = base.tokens;
		this.lines = base.lines;
		this.charpos = base.charpos;
		this.bytes = base.bytes;
		this.bytepos = base.bytepos;
		this.start = start;
		this.end = end;
	}
	
	/**
	 * @return the number of tokens in this sequence.
	 */
	public int size() {
		return end - start;
	}
	
	/**
	 * Get the line number of a specified token. 
	 * @param pos index of a token.
	 * @return a line number.
	 */
	public int getLine(int pos) {
		return lines.get(pos + start);
	}
	
	/**
	 * Get the character position in the line of a specified token. 
	 * @param pos index of a token.
	 * @return a char position in a line. 
	 * The value may be different from a visible position in an editor, 
	 * because it regards any UNICODE character (including a tab) as one character.
	 */
	public int getCharPositionInLine(int pos) {
		return charpos.get(pos + start);
	}
	
	/**
	 * Get the last character position in the line of a specified token.
	 * Exclusive.
	 * @param pos
	 * @return
	 */
	public int getEndCharPositionInLine(int pos) {
		return charpos.get(pos + start) + getToken(pos).length();
	}
	
	/**
	 * Get the string content of a specified token.
	 * @param pos index of a token.
	 * @return the conent of the token.
	 */
	public String getToken(int pos) {
		return tokens.get(pos + start);
	}
	
	/**
	 * Extract a substring of tokens.
	 * @param start
	 * @param end specifies the index of the end of tokens.  Exclusive.
	 * @return tokens.  The method returns null if invalid parameters are specified. 
	 */
	public TokenSequence substring(int start, int end) {
		if (0 <= start && start < size() &&
			start < end && end <= size()) {
			return new TokenSequence(this, this.start + start, this.start + end);
		} else {
			return null;
		}
	}
	
	/**
	 * Extract a substring of tokens between the specifid lines.
	 * @param startLine specifies the start line of tokens.
	 * @param endLine specifies the end line of tokens.  Differently from substring method, the line is included in the substring.
	 * @return tokens.  The method returns null if invalid lines are specifid (e.g. startLine is greater than endLine).
	 */
	public TokenSequence substringByLine(int startLine, int endLine) {
		int startPos;
		if (startLine > endLine) {
			return null;
		}
		int pos;
		if (startLine <= getLine(0)) { 
			startPos = 0;
			pos = 1;
		} else {
			pos = 1;
			while (pos<size()) {
				if (getLine(pos-1) < startLine && startLine <= getLine(pos)) {
					break;
				}
				pos++;
			}
			if (pos < size()) startPos = pos;
			else return null;
		}
		
		while (pos<size()) {
			if (getLine(pos-1) <= endLine && endLine < getLine(pos)) {
				break;
			}
			pos++;
		}
		return new TokenSequence(this, startPos, pos);
	}

	/**
	 * Return a byte array including tokens.
	 * The byte array should not be modified, because 
	 * an internal cache may be corrupted.
	 * @return
	 */
	public byte[] toByteArray() {
		if (start == 0 && end == tokens.size()) {
			return bytes;
		} else {
			return Arrays.copyOfRange(bytes, bytepos.get(start), bytepos.get(end));
		}
	}

	/**
	 * @return token positions that are the first tokens of lines, 
	 * i.e. each pos in the resultant array satisfies: getLine(pos-1) < getLine(pos).
	 */
	public int[] getLineHeadTokenPositions() {
		TIntArrayList result = new TIntArrayList();
		for (int i=0; i<size(); i++) {
			if (i == 0 || getLine(i-1) < getLine(i)) {
				result.add(i);
			}
		}
		return result.toArray();
	}
	
	/**
	 * @param windowSize
	 * @return every token positions to be compared with a query. 
	 * If windowSize is larger than the token sequence,
	 * the entire token sequence is compared. 
	 */
	public int[] getFullPositions(int windowSize) {
		int length = size() - windowSize + 1;
		if (length < 1) length = 1;
		int[] result = new int[length];
		for (int i=0; i<result.length; i++) {
			result[i] = i;
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int i=0; i<size(); i++) {
			b.append(getToken(i));
			b.append(" ");
		}
		return b.toString();
	}

}
