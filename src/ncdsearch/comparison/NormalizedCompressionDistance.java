package ncdsearch.comparison;

import ncdsearch.comparison.ncd.DeflateStrategy;
import ncdsearch.comparison.ncd.ICompressionStrategy;


/**
 * This class provides a common strategy to calculate a Normalized Compression Distance
 * using a compression algorithm.
 */
public class NormalizedCompressionDistance implements ICodeDistanceStrategy {

	/**
	 * An object specifying a compression algorithm.
	 */
	private ICompressionStrategy strategy;
	
	/**
	 * The byte array of a query code fragment
	 */
	private byte[] baseBytes;
	
	/**
	 * The size of a compressed form of the code fragment
	 */
	private long baseSize;
	
	/**
	 * Create a distance calculation object.
	 * A client must close the object to release an internal resource.   
	 * @param query specifies a preset query.  The query is reused for multiple ncd method calls.
	 */
	public NormalizedCompressionDistance(TokenSequence query) {
		this(query, new DeflateStrategy());
	}

	public NormalizedCompressionDistance(TokenSequence query, ICompressionStrategy strategy) {
		this.strategy = strategy;
		
		baseBytes = query.toByteArray();
		baseSize = strategy.getDataSize(baseBytes, 0, baseBytes.length);
	}
	
	/**
	 * Release an internal resource.
	 */
	@Override
	public void close() {
		strategy.close();
	}
	
	/**
	 * Compute a distance between a preset query and a given target file
	 * using the definition of Normalized Compression Distance.
	 */
	@Override
    public double computeDistance(TokenSequence target) {
    	byte[] targetBytes = target.toByteArray(); 
   		byte[] result = new byte[baseBytes.length + targetBytes.length];
    	System.arraycopy(baseBytes, 0, result, 0, baseBytes.length);
    	System.arraycopy(targetBytes, 0, result, baseBytes.length, targetBytes.length);

    	long c1and2 = strategy.getDataSize(result, 0, result.length);
    	long c2 = strategy.getDataSize(targetBytes, 0, targetBytes.length);
        return (c1and2 - Math.min(baseSize, c2)) * 1.0 / Math.max(baseSize, c2);
    }
    
}
