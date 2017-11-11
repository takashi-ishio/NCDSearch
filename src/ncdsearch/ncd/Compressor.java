package ncdsearch.ncd;

public enum Compressor {

	ZIP, XZ, ZSTD;
	
	public static ICompressionStrategy createInstance(Compressor c) {
		if (c != null) {
			switch (c) {
			case ZIP: return new DeflateStrategy();
			case XZ: return new XzStrategy();
			case ZSTD: return new ZstdStrategy();
			}
		}
		return new DeflateStrategy();
	}

}
