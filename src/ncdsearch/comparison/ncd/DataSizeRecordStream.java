package ncdsearch.comparison.ncd;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This output stream measures the data size and discards the actual data. 
 */
public class DataSizeRecordStream extends OutputStream { 

	private int size = 0;
	
	/**
	 * Create a stream for measuring the data size.
	 */
	public DataSizeRecordStream() {
	}
	
	public void reset() {
		size = 0;
	}

	@Override
	public void write(int b) throws IOException {
		size++;
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		size += len;
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		size += b.length;
	}
	
	public int size() {
		return size;
	}
    	
}
