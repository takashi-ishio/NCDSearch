package ncdsearch.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ncdsearch.comparison.TokenSequence;

public class Fragment implements Comparable<Fragment> {

	public enum LinkStyle { None, Eclipse, VSCode, FileURL;

		/**
		 * Translate a style name into a link style.
		 * @param name can be "eclipse", "vscode", and "fileurl".
		 * @return a link style object.
		 */
		public static LinkStyle parse(String name) {
			if (name.equalsIgnoreCase("eclipse")) {
				return Eclipse;
			} else if (name.equalsIgnoreCase("vscode")) {
				return VSCode;
			} else if (name.equalsIgnoreCase("fileurl")) {
				return FileURL;
			} else {
				return None;
			}
		}
		
		/**
		 * Produce a file name in a hyperlink style
		 * @param filename
		 * @param line
		 * @return a link to open the specified file and line number
		 */
		public String format(String filename, int line) {
			switch (this) {
			case Eclipse:
				return "(" + new File(filename).getName() + ":" + line + ")";
			case VSCode:
				return filename + ":" + line;
			case FileURL:
				return "file://" + filename;
			default:
				return filename;
			}
		}
	}; 	

	
	private static final String SEPARATOR = ",";

	private String filename;
	private TokenSequence fileTokens;
	private int startPos;
	private int endPos;
	private double distance;

	/**
	 * @param filename
	 * @param startPos
	 * @param endPos   exclusive.
	 * @param distance
	 */
	public Fragment(String filename, TokenSequence fileTokens, int startPos, int endPos, double distance) {
		this.filename = filename;
		this.fileTokens = fileTokens;
		this.startPos = startPos;
		this.endPos = endPos;
		this.distance = distance;
		assert this.startPos < this.endPos : "Zero-length fragment is not allowed.";
	}

	/**
	 * @return a string representation of the position and the distance.
	 */
	public String toString() {
		return toString(false, LinkStyle.FileURL);
	}

	/**
	 * @return a string representation of the position and the distance. 
	 * It includes
	 *         character positions in lines to analyze details.
	 */
	public String toLongString() {
		return toString(true, LinkStyle.None);
	}

	/**
	 * @param detail If this flag is set to true, the resultant string includes character positions in lines.
	 * @param style specifies a format of file name so that a particular editor/console can automatically hyperlink the file name. 
	 * @return a string representation of the position and the distance.
	 */
	public String toString(boolean detail, LinkStyle style) {
		StringBuilder b = new StringBuilder();
		if (style != null) {
			b.append(style.format(filename, getStartLine()));
		} else {
			b.append(filename);
		}
		b.append(SEPARATOR);
		b.append(getStartLine());
		if (detail) {
			b.append(SEPARATOR);
			b.append(getStartCharPositionInLine());
		}
		b.append(SEPARATOR);
		b.append(getEndLine());
		if (detail) {
			b.append(SEPARATOR);
			b.append(getEndCharPositionInLine());
		}
		b.append(SEPARATOR);
		b.append(distance);
		b.append("\n");
		return b.toString();
	}

	/**
	 * @return the file name of this code fragment. 
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the first line number of this code fragment. 
	 */
	public int getStartLine() {
		return fileTokens.getLine(startPos);
	}

	/**
	 * @return the last line number of this code fragment. 
	 */
	public int getEndLine() {
		return fileTokens.getLine(endPos - 1);
	}

	/**
	 * @return the first character position in the line of this code fragment. 
	 */
	public int getStartCharPositionInLine() {
		return fileTokens.getCharPositionInLine(startPos);
	}

	/**
	 * @return the last character position in the line of this code fragment. 
	 */
	public int getEndCharPositionInLine() {
		return fileTokens.getEndCharPositionInLine(endPos - 1);
	}

	/**
	 * @return the distance between this code fragment and the query code fragment.
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @return a string representation of tokens included in the code fragment.
	 */
	public String getTokenString() {
		return fileTokens.substring(startPos, endPos).toString();
	}

	/**
	 * Checks if two code fragments are overlapping with each other.
	 * This method is used for excluding overlapping code fragments from a result.
	 * @param another fragment.
	 * @return true if this fragment overlaps with another one.
	 */
	public boolean overlapWith(Fragment another) {
		return !(this.endPos <= another.startPos || another.endPos <= this.startPos);
	}

	/**
	 * Compare two fragments and select a better one.
	 * 
	 * @param another
	 * @return true if this object is better for output. False if another is better.
	 *         A fragment is better than another one if it has a lower distance. If
	 *         tied, shorter is better. If they have the same legnth, smaller start
	 *         position is better.
	 */
	public boolean isBetterThan(Fragment another) {
		// Distance: Lower is better
		if (this.distance < another.distance)
			return true;
		else if (this.distance > another.distance)
			return false;
		else {
			// Shorter is better
			int thislen = this.endPos - this.startPos;
			int anotherlen = another.endPos - another.startPos;
			if (thislen < anotherlen)
				return true;
			else if (thislen > anotherlen)
				return false;
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
	 * Remove redundant elements by selecting best fragments in a greedy manner
	 * 
	 * @param fragments a list of fragments to be processed. This collection is
	 *                  modified by this method.
	 * @return a filtered list of fragments.
	 */
	public static ArrayList<Fragment> filter(ArrayList<Fragment> fragments) {
		fragments.sort(new Comparator<Fragment>() {
			@Override
			public int compare(Fragment o1, Fragment o2) {
				if (o1.isBetterThan(o2))
					return -1;
				else
					return 1;
			}
		});
		ArrayList<Fragment> result = new ArrayList<>();
		for (int i = 0; i < fragments.size(); i++) {
			Fragment f1 = fragments.get(i);
			if (f1 == null)
				continue;

			result.add(f1);
			for (int j = i + 1; j < fragments.size(); j++) {
				Fragment f2 = fragments.get(j);
				if (f2 == null)
					continue;
				if (f1.overlapWith(f2)) {
					fragments.set(j, null);
				}
			}
		}
		Collections.sort(result);
		return result;
	}

}
