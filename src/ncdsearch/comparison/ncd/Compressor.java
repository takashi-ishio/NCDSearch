package ncdsearch.comparison.ncd;

import java.util.zip.Deflater;

public enum Compressor {

	ZIP, DEFLATE, XZ, ZSTD, FOLCA, BZIP2, SNAPPY, ZIPHIGH;
	
	public static ICompressionStrategy createInstance(Compressor c) {
		if (c != null) {
			switch (c) {
			case ZIP: 
			case DEFLATE: 
				return new DeflateStrategy();
			case ZIPHIGH: return new DeflateStrategy(Deflater.BEST_COMPRESSION);
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
