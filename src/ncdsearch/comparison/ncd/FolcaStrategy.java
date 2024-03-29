package ncdsearch.comparison.ncd;


import ncdsearch.comparison.ncd.folca.FOLCA;

/**
 * This class is a FOLCA client for NCD calculation
 */
public class FolcaStrategy implements ICompressionStrategy {

	public FolcaStrategy() {
	}
	
	@Override
	public long getDataSize(byte[] buf, int start, int length) {
		FOLCA f = new FOLCA();
		f.process(buf, start, length);
		f.finish();
		return f.getDictionarySize();
	}
	
	@Override
	public void close() {
	}
}
