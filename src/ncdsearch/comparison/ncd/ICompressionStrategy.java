package ncdsearch.comparison.ncd;

public interface ICompressionStrategy {

	/**
	 * Compress buffer data to approximate its Kolmogorov complexity.
	 * @param buf
	 * @param start
	 * @param length
	 * @return the data size compressed by the strategy object.
	 */
	public long getDataSize(byte[] buf, int start, int length);

	/**
	 * Release internal resources of the object. 
	 */
	public void close();
}
