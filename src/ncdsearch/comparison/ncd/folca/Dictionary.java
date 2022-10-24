package ncdsearch.comparison.ncd.folca;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongIntHashMap;

/**
 * This dictionary object is to record a pair of integer symbols as a new symbol
 * according to the FOLCA algorithm.
 */
public class Dictionary {

	private static int SYMBOL_START = 256; 
	private int nextSymbol = SYMBOL_START;
	private TLongIntHashMap symbols;
	private TLongArrayList tree;
	private Integer root;
	
	public Dictionary() {
		symbols = new TLongIntHashMap(65536);
		tree = new TLongArrayList(65536);
	}
	
	
	public void setRoot(Integer root) {
		this.root = root;
	}
	
	public int getSymbol(int first, int second) {
		long l = encode(first, second);
		if (symbols.contains(l)) {
			return symbols.get(l);
		} else {
			int s = nextSymbol++;
			symbols.put(l, s);
			tree.add(l);
			assert s-SYMBOL_START == tree.size()-1;
			return s;
		}
	}

	private long encode(int first, int second) { 
		return (((long)first) << 32) | (long)second;
	}
	

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<tree.size(); i++) {
			buf.append(i + SYMBOL_START); 
			buf.append(" -> ");
			buf.append(firstSymbol(tree.get(i)));
			buf.append(", ");
			buf.append(secondSymbol(tree.get(i)));
			buf.append("; ");			
		}
		return buf.toString();
	}
	
	public boolean isTerminal(int symbol) { 
		return symbol < SYMBOL_START;
	}
	
	private int firstSymbol(long l) {
		return (int)(l >> 32);
	}
	
	private int secondSymbol(long l) {
		return (int)(l & Integer.MAX_VALUE); 
	}
	
	public int decodeFirst(int symbol) {
		assert symbol >= SYMBOL_START;
		long l = tree.get(symbol-SYMBOL_START);
		return firstSymbol(l);
	}
	
	public int decodeSecond(int symbol) {
		assert symbol >= SYMBOL_START;
		long l = tree.get(symbol-SYMBOL_START);
		return secondSymbol(l);
	}
	
	public byte[] decode() {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		if (root != null) {
			decode(buf, root.intValue());
		}
		return buf.toByteArray();
	}
	
	public byte[] decode(int symbol) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		decode(buf, symbol);
		return buf.toByteArray();
	}
	
	private void decode(ByteArrayOutputStream buf, int symbol) { 
		if (isTerminal(symbol)) {
			buf.write(symbol);
		} else {
			int s1 = decodeFirst(symbol);
			decode(buf, s1);
			int s2 = decodeSecond(symbol);
			decode(buf, s2);
		}
	}
	
	public int size() {
		return tree.size();
	}
	
	public HashSet<String> getStrings() {
		HashSet<String> set = new HashSet<>();
		for (int i=0; i<tree.size(); i++) {
			String k = new String(decode(i + SYMBOL_START));
			set.add(k);
		}
		return set;
	}
	
}
