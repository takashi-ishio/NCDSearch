package ncdsearch.ncd;

import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


public class DeflateStrategy implements ICompressionStrategy {

	private Deflater deflater;
	private DataSizeRecordStream sizeRecorder;
	
	public DeflateStrategy() {
		deflater = new Deflater();
		sizeRecorder = new DataSizeRecordStream();
	}
	
	@Override
	public void close() {
		deflater.end();
	}
	
	@Override
    public long getDataSize(byte[] buf, int start, int length) {
    	deflater.reset();
    	sizeRecorder.reset();
    	DeflaterOutputStream out = new DeflaterOutputStream(sizeRecorder, deflater);
    	try {
    		out.write(buf, start, length);
	    	out.close();
	    	return sizeRecorder.size();
    	} catch (IOException e) {
    		assert false: "SizeRecorder never throws IOException.";
    		return 0;
    	}
    }


}
