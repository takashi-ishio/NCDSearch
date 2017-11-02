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
		baseSize = getCompressedDataSize(query, null);
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
        long c2 = getCompressedDataSize(target, null);
        long c1and2 = getCompressedDataSize(query, target);
        return (c1and2 - Math.min(baseSize, c2)) * 1.0 / Math.max(baseSize, c2);
    }

    /**
     * Compute a distance between given two files.
     * This method does not use a preset query.
     * @param s1
     * @param s2
     * @return
     */
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
