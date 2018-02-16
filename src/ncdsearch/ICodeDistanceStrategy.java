package ncdsearch;

public interface ICodeDistanceStrategy extends AutoCloseable {

	/**
	 * Compute a similarity with a token sequence.
	 * The method assumes that the instance already has a query token sequence.
	 * @param code
	 * @return a distance value.  
	 */
	public double computeDistance(TokenSequence code);
	
	/**
	 * Release an internal resource if the implementation has.
	 */
	public void close();
	
}
