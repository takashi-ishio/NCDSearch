package ncdsearch.ncd;

import java.io.IOException;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class XzStrategy implements ICompressionStrategy {

	private DataSizeRecordStream sizeRecorder;
	private LZMA2Options xzOptions;
	
	
	public XzStrategy() {
		sizeRecorder = new DataSizeRecordStream();
		xzOptions = new LZMA2Options();
	}
	
	@Override
	public long getDataSize(byte[] buf, int start, int length) {
		sizeRecorder.reset();
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
