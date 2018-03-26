package ncdsearch.ncd;

import java.io.IOException;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;


public class Bzip2Strategy implements ICompressionStrategy {

	private DataSizeRecordStream sizeRecorder;

	public Bzip2Strategy() {
		sizeRecorder = new DataSizeRecordStream();
	}
	
	@Override
	public long getDataSize(byte[] buf, int start, int length) {
		sizeRecorder.reset();
		try {
			BZip2CompressorOutputStream out = new BZip2CompressorOutputStream(sizeRecorder);
			out.write(buf, start, length);
			out.finish();
			out.close();
			return sizeRecorder.size();
		} catch (IOException e) {
			return 0;
		}
	}
	
	@Override
	public void close() {
	}
}
