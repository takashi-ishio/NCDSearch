package ncdsearch.comparison;

/**
 * An interface representing a code comparison strategy.
 * This object calculates the best tokens in a target source code fragment
 * that is closest to a query code fragment. 
 */
public interface IVariableWindowStrategy extends ICodeDistanceStrategy {

	/**
	 * 
	 * @param code specifies a token sequence
	 * @param startPos is the first token of a target fragment in the token sequence
	 * @param endPos is the last token (exclusive) of a target fragment in the token sequence
	 * @param threshold is a threshold of distance.  
	 * This method does not report a code fragment whose distance is greater than this threshold. 
	 * @return the best distance between a substring in the target code fragment and a query code fragment.
	 */
	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold);
	
	/**
	 * @return the length of the best substring returned by findBestMatch method.
	 */
	public int getBestWindowSize();

}
