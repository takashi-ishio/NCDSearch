package ncdsearch.ncd;

public interface ICompressionStrategy {

	/**
	 * Compress buffer data to approximate its Kolmogorov complexity.
	 * @param buf
	 * @param start
	 * @param length
	 * @return
	 */
	public long getDataSize(byte[] buf, int start, int length);

	public void close();
}
