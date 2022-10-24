package ncdsearch.comparison.ncd;

import java.io.IOException;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZOutputStream;

/**
 * This class is a XZ client for NCD calculation
 */
public class XzStrategy implements ICompressionStrategy {

	private LZMA2Options xzOptions;
	
	
	public XzStrategy() {
		try {
			xzOptions = new LZMA2Options(0);
		} catch (UnsupportedOptionsException e) {
		}
	}
	
	@Override
	public long getDataSize(byte[] buf, int start, int length) {
		DataSizeRecordStream sizeRecorder = new DataSizeRecordStream();
		try {
			XZOutputStream stream = new XZOutputStream(sizeRecorder, xzOptions);
			stream.write(buf, start, length);
			stream.close();
			return sizeRecorder.size();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	public void close() {
	}
	
}
