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
	private TokenSequence query;
	private long baseSize;
	
	/**
	 * Create a distance computation object.
	 * A client must close the object to release an internal resource.   
	 * @param query specifies a preset query.  The query is reused for multiple ncd method calls.
	 */
	public NormalizedCompressionDistance(TokenSequence query) {
		deflater = new Deflater();
		sizeRecorder = new DeflateSize();
		this.query = query;
		
		byte[] b = query.toByteArray();
		baseSize = getCompressedDataSize(b, 0, b.length);
	}
	
	/**
	 * Release an internal resource.
	 */
	@Override
	public void close() {
		deflater.end();
	}
	
	/**
	 * Compute a distance between a preset query and a given target file. 
	 */
    public double ncd(TokenSequence target) {
    	byte[] b = query.concat(target);
    	long c1and2 = getCompressedDataSize(b, 0, b.length);
    	long c2 = getCompressedDataSize(b, query.toByteArray().length, b.length - query.toByteArray().length);
        return (c1and2 - Math.min(baseSize, c2)) * 1.0 / Math.max(baseSize, c2);
    }

    /**
     * Compute a compressed data size for the query and a given file.
     * @param s1
     * @param s2
     * @return
     */
    private long getCompressedDataSize(byte[] buf, int start, int length) {
    	deflater.reset();
    	sizeRecorder.reset();
    	DeflaterOutputStream out = new DeflaterOutputStream(sizeRecorder, deflater);
    	try {
    		out.write(buf, start, length);
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
