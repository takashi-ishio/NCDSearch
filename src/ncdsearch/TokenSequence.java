package ncdsearch;

import java.util.ArrayList;

import gnu.trove.list.array.TIntArrayList;
import sarf.lexer.TokenReader;

public class TokenSequence {

	private ArrayList<String> tokens;
	private TIntArrayList lines;
	private TIntArrayList charpos;
	private int start;
	private int end;
	private ArrayList<byte[]> bytes;
	
	public TokenSequence(TokenReader r) {
		tokens = new ArrayList<>();
		lines = new TIntArrayList();
		charpos = new TIntArrayList();
		bytes = new ArrayList<>();
		while (r.next()) {
			tokens.add(r.getToken());
			lines.add(r.getLine());
			charpos.add(r.getCharPositionInLine());
			bytes.add(r.getToken().getBytes());
		}
		start = 0;
		end = tokens.size();
	}
	
	private TokenSequence(TokenSequence s, int start, int end) { 
		this.tokens = s.tokens;
		this.lines = s.lines;
		this.charpos = s.charpos;
		this.bytes = s.bytes;
		this.start = start;
		this.end = end;
	}
	
	public int size() {
		return end - start;
	}
	
	public int getLine(int pos) {
		return lines.get(pos + start);
	}
	
	public int getCharPositionInLine(int pos) {
		return charpos.get(pos + start);
	}
	
	public String getToken(int pos) {
		return tokens.get(pos + start);
	}
	
	public byte[] getTokenBytes(int pos) {
		return bytes.get(pos + start);
	}
	
	/**
	 * Extract a substring of tokens.
	 * @param start
	 * @param end specifies the index of the end of tokens.  Exclusive.
	 * @return tokens.  The method returns null if invalid parameters are specified. 
	 */
	public TokenSequence substring(int start, int end) {
		if (0 <= start && start < tokens.size() &&
			1 <= end && end <= tokens.size() &&
			start < end) {
			return new TokenSequence(this, start, end);
		} else {
			return null;
		}
	}
	
	/**
	 * Create byte[] including token data.
	 * This method constructs a new array for each call.
	 * @return
	 */
	public byte[] toByteArray() {
		int len = 0;
		for (int i=start; i<end; i++) {
			len += bytes.get(i).length + 1;
		}
		byte[] buf = new byte[len];
		int pos = 0;
		for (int i=start; i<end; i++) {
			byte[] b = bytes.get(i);
			System.arraycopy(b, 0, buf, pos, b.length);
			pos += b.length + 1;
		}
		return buf;
	}

}
