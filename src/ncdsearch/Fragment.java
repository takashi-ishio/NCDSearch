package ncdsearch;

import java.util.ArrayList;
import java.util.Collections;


public class Fragment implements Comparable<Fragment> {

	private static final String SEPARATOR = ",";

	private String filename;
	private TokenSequence tokens;
	private int startPos;
	private int endPos;
	private double distance;
	
	/**
	 * @param filename
	 * @param startPos
	 * @param endPos exclusive.
	 * @param distance
	 */
	public Fragment(String filename, TokenSequence tokens, int startPos, int endPos, double distance) {
		this.filename = filename;
		this.tokens = tokens;
		this.startPos = startPos;
		this.endPos = endPos;
		this.distance = distance;
		assert this.startPos < this.endPos: "Zero-length fragment is not allowed.";
	}
	
	/**
	 * @return a string represenation of the position and the distance.
	 */
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(filename);
		b.append(SEPARATOR);
		b.append(tokens.getLine(startPos));
		b.append(SEPARATOR);
		b.append(tokens.getLine(endPos-1));
		b.append(SEPARATOR);
        b.append(distance);
        return b.toString();
	}
	
	/**
	 * @return a string represenation of the position and the distance.
	 * It includes character positions in lines to analyze details.
	 */
	public String toLongString() {
		StringBuilder b = new StringBuilder();
		b.append(filename);
		b.append(SEPARATOR);
		b.append(tokens.getLine(startPos));
		b.append(SEPARATOR);
        b.append(tokens.getCharPositionInLine(startPos)); 
		b.append(SEPARATOR);
		b.append(tokens.getLine(endPos-1));
		b.append(SEPARATOR);
        b.append(tokens.getEndCharPositionInLine(endPos-1)); 
		b.append(SEPARATOR);
        b.append(distance);
        return b.toString();
	}
	
	public boolean overlapWith(Fragment another) {
		return !(this.endPos <= another.startPos ||
			another.endPos <= this.startPos);
	}
	
	/**
	 * Compare two overlapping fragments and select a better one.
	 * @param another
	 * @return true if this object is better for output. 
	 * False if another is better.
	 */
	public boolean isBetterThan(Fragment another) {
		assert this.overlapWith(another);
		// Distance: Lower is better
		if (this.distance < another.distance) return true;
		else if (this.distance > another.distance) return false;
		else {
			// Shorter is better
			int thislen = this.endPos - this.startPos;
			int anotherlen = another.endPos - another.startPos;
			if (thislen < anotherlen) return true;
			else if (thislen > anotherlen) return false;
			else {
				return this.startPos < another.startPos;
			}
		}
	}
	
	/**
	 * Sort fragments by their starting positions in the ascending order.
	 */
	@Override
	public int compareTo(Fragment another) {
		return this.startPos - another.startPos;
	}
	
	/**
	 * Remove redundant elements.
	 * @param fragments a list of fragments to be processed.  
	 * This collection is modified by this method.
	 * @return a filtered list of fragments.
	 */
	public static ArrayList<Fragment> filter(ArrayList<Fragment> fragments) {
		for (int i=0; i<fragments.size(); i++) {
			Fragment f1 = fragments.get(i);
			if (f1 == null) continue;
			for (int j=i+1; j<fragments.size(); j++) {
				Fragment f2 = fragments.get(j);
				if (f2 == null) continue;
				if (f1.overlapWith(f2)) {
					if (f1.isBetterThan(f2)) {
						fragments.set(j, null);
					} else {
						fragments.set(i, null);
						break;
					}
				}
			}
		}
		ArrayList<Fragment> result = new ArrayList<>();
		for (int i=0; i<fragments.size(); i++) {
			if (fragments.get(i) != null) {
				result.add(fragments.get(i));
			}
		}
		Collections.sort(result);
		return result;
	}

}
