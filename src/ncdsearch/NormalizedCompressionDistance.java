package ncdsearch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * https://github.com/jmmcd/GPDistance/blob/master/java/TreeDistance/NormalisedCompressionDistance.java
 * @author ishio
 *
 */
public class NormalizedCompressionDistance implements AutoCloseable {

	private Deflater deflater;
	private DeflateSize sizeRecorder;
	
	public NormalizedCompressionDistance() {
		deflater = new Deflater();
		sizeRecorder = new DeflateSize();
	}
	
	@Override
	public void close() {
		deflater.end();
	}
	
    public double ncd(TokenSequence s1, TokenSequence s2) {
        long c1 = getCompressedDataSize(s1, null);
        long c2 = getCompressedDataSize(s2, null);
        long c1and2 = getCompressedDataSize(s1, s2);
        return (c1and2 - Math.min(c1, c2)) * 1.0 / Math.max(c1, c2);
    }

    public long getCompressedDataSize(TokenSequence s1, TokenSequence s2) {
    	
    	deflater.reset();
    	sizeRecorder.reset();
    	DeflaterOutputStream out = new DeflaterOutputStream(sizeRecorder, deflater);
    	try {
    		for (int i=0; i<s1.size(); i++) {
    	    	out.write(s1.getTokenBytes(i));
    	    	out.write(0);
    		}
	    	if (s2 != null) {
	    		for (int i=0; i<s2.size(); i++) {
	    	    	out.write(s2.getTokenBytes(i));
	    	    	out.write(0);
	    		}
	    	}
	    	out.close();
	    	return sizeRecorder.size;
    	} catch (IOException e) {
    		assert false: "SizeRecorder never throws IOException.";
    		return 0;
    	}
    }

    private static class DeflateSize extends OutputStream { 

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
    	
    	
    }
    
}
