package ncdsearch.comparison.ncd;

import java.io.IOException;
import java.io.OutputStream;

public class DataSizeRecordStream extends OutputStream { 

	private int size = 0;
	
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
