package ncdsearch.ncd;

public enum Compressor {

	ZIP, XZ, ZSTD, FOLCA, BZIP2, SNAPPY;
	
	public static ICompressionStrategy createInstance(Compressor c) {
		if (c != null) {
			switch (c) {
			case ZIP: return new DeflateStrategy();
			case XZ: return new XzStrategy();
			case ZSTD: return new ZstdStrategy();
			case FOLCA: return new FolcaStrategy();
			case BZIP2: return new Bzip2Strategy();
			case SNAPPY: return new SnappyStrategy();
			}
		}
		return new DeflateStrategy();
	}

}
