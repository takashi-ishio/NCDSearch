package ncdsearch.ncd;

import java.io.IOException;

import org.xerial.snappy.SnappyOutputStream;

public class SnappyStrategy implements ICompressionStrategy {

	private DataSizeRecordStream sizeRecorder;
	
	public SnappyStrategy() {
		sizeRecorder = new DataSizeRecordStream();
	}
	
	@Override
	public long getDataSize(byte[] buf, int start, int length) {
		sizeRecorder.reset();
		try {
			SnappyOutputStream stream = new SnappyOutputStream(sizeRecorder);
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
