package ncdsearch.ncd;

import java.io.IOException;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZOutputStream;

public class XzStrategy implements ICompressionStrategy {

	private DataSizeRecordStream sizeRecorder;
	private LZMA2Options xzOptions;
	private XZOutputStream stream;
	
	public XzStrategy() {
		sizeRecorder = new DataSizeRecordStream();
		xzOptions = new LZMA2Options();
		try {
			stream = new XZOutputStream(sizeRecorder, xzOptions);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public long getDataSize(byte[] buf, int start, int length) {
		sizeRecorder.reset();
		try {
			stream.write(buf, start, length);
			stream.finish();
			return sizeRecorder.size();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	public void close() {
		try {
			stream.close();
		} catch (IOException e){ 
		}
	}
	
}
