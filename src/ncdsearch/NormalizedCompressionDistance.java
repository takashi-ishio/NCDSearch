package ncdsearch;

import ncdsearch.ncd.DeflateStrategy;
import ncdsearch.ncd.ICompressionStrategy;


public class NormalizedCompressionDistance implements ICodeDistanceStrategy {

	private ICompressionStrategy strategy;
	private TokenSequence query;
	private long baseSize;
	
	/**
	 * Create a distance computation object.
	 * A client must close the object to release an internal resource.   
	 * @param query specifies a preset query.  The query is reused for multiple ncd method calls.
	 */
	public NormalizedCompressionDistance(TokenSequence query) {
		this(query, new DeflateStrategy());
	}

	public NormalizedCompressionDistance(TokenSequence query, ICompressionStrategy strategy) {
		this.query = query;
		this.strategy = strategy;
		
		byte[] b = query.toByteArray();
		baseSize = strategy.getDataSize(b, 0, b.length);
	}
	
	/**
	 * Release an internal resource.
	 */
	@Override
	public void close() {
		strategy.close();
	}
	
	/**
	 * Compute a distance between a preset query and a given target file. 
	 */
	@Override
    public double computeDistance(TokenSequence target) {
    	byte[] b = query.concat(target);
    	long c1and2 = strategy.getDataSize(b, 0, b.length);
    	long c2 = strategy.getDataSize(b, query.toByteArray().length, b.length - query.toByteArray().length);
        return (c1and2 - Math.min(baseSize, c2)) * 1.0 / Math.max(baseSize, c2);
    }
    
	@Deprecated
	public double ncd(TokenSequence target) {
		return computeDistance(target);
	}
	
}
