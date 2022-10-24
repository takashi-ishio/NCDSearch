package ncdsearch.comparison;

/**
 * An interface representing a code comparison strategy.
 * This object calculates a distance between a query code fragment and a target source code fragment. 
 */
public interface ICodeDistanceStrategy extends AutoCloseable {

	/**
	 * Compute a similarity with a token sequence.
	 * The method assumes that the instance already has a query token sequence.
	 * @param code is the token sequence of a target code fragment.
	 * @return a distance value.  
	 */
	public double computeDistance(TokenSequence code);
	
	/**
	 * Release an internal resource if the implementation has.
	 */
	public void close();
	
}
