package ncdsearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;
import sarf.lexer.TokenReader;


/**
 * This class represents a code fragment.
 * This is the main data structure of NCDSearch.
 */
public class TokenSequence {

	private static final int SEPARATOR = 0;
	
	/**
	 * List of tokens in this code fragment
	 */
	private ArrayList<String> tokens;
	
	/**
	 * Line number attribute of each token
	 */
	private TIntArrayList lines;
	
	/**
	 * The character position in line of each token
	 */
	private TIntArrayList charpos;
	
	/**
	 * An index of the first token of the code fragment.
	 * This field enables multiple TokenSequence objects share 
	 * the same ArrayList including tokens. 
	 */
	private int start;
	
	/**
	 * An index of the last token (exclusive) of the code fragment.
	 * This field enables multiple TokenSequence objects share 
	 * the same ArrayList including tokens. 
	 */
	private int end;
	
	/**
	 * A byte array representation for code comparison
	 */
	private byte[] bytes;
	
	/**
	 * Positions in the byte array corresponding to tokens
	 */
	private TIntArrayList bytepos;
	
	/**
	 * This is a flag representing whether a separator is 
	 * inserted between tokens in the byte array or not.
	 */
	private boolean useSeparator;

	/**
	 * Create an object including all tokens obtained from a reader.
	 * A separator is inserted between tokens.
	 * @param r specifies a TokenReader.
	 * @param normalization If true, normalize identifier tokens.
	 */
	public TokenSequence(TokenReader r, boolean normalization) {
		this(r, normalization, true);
	}

	/**
	 * Create an object including all tokens obtained from a reader.
	 * @param r specifies a TokenReader.
	 * @param normalization If true, use a normalizer. 
	 * @param separator If true, a separator is inserted between tokens.
	 */
	public TokenSequence(TokenReader r, boolean normalization, boolean separator) {
		tokens = new ArrayList<>();
		lines = new TIntArrayList();
		charpos = new TIntArrayList();
		bytepos = new TIntArrayList();
		useSeparator = separator;
		ByteArrayOutputStream s = new ByteArrayOutputStream(65536);
		try {
			while (r.next()) {
				bytepos.add(s.size());
				String t = normalization ? r.getNormalizedToken() : r.getToken();  
				tokens.add(t);
				s.write(t.getBytes());
				if (separator) {
					s.write(SEPARATOR);
				}
				lines.add(r.getLine());
				charpos.add(r.getCharPositionInLine());
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
		this.useSeparator = base.useSeparator;
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Wrap a string as a single-token sequence
	 * @param s specifies a string.
	 * @return a TokenSequence object.
	 */
	public TokenSequence(String s) {
		tokens = new ArrayList<>();
		tokens.add(s);
		lines = new TIntArrayList();
		lines.add(0);
		charpos = new TIntArrayList();
		charpos.add(0);
		start = 0;
		end = 1;
		bytes = s.getBytes();
		bytepos = new TIntArrayList();
		bytepos.add(0);
		bytepos.add(bytes.length);
		useSeparator = false;
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
	 * @return the content of the token.
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
	 * Extract a substring of tokens between the specified lines.
	 * @param startLine specifies the start line of tokens.
	 * @param endLine specifies the end line of tokens.  Differently from substring method, the line is included in the substring.
	 * @return tokens.  The method returns null if invalid lines are specified (e.g. startLine is greater than endLine).
	 */
	public TokenSequence substringByLine(int startLine, int endLine) {
		int startPos;
		if (startLine > endLine || size() == 0) {
			return null;
		}
		int pos;
		if (endLine < getLine(0)) { 
			return null;
		} else if (startLine <= getLine(0)) { 
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
	 * @return an array of token positions in a byte array returned by toByteArray().
	 * This array includes an additional position indicating the end position of the last token.
	 */
	public int[] getBytePositions() {
		if (start == 0 && end == tokens.size()) {
			return bytepos.toArray();
		}
		return bytepos.toArray(start, end-start+1);
	}
	
	/**
	 * @param pos index of a token.
	 * @return a position of the token in the internal byte array.
	 */
	public int getBytePosition(int pos) {
		return bytepos.get(pos + start);
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
	
	/**
	 * @return a string representation of tokens.
	 * The string is separated by white space if separator is enabled. 
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int i=0; i<size(); i++) {
			b.append(getToken(i));
			if (useSeparator) {
				b.append(" ");
			}
		}
		return b.toString();
	}

}
