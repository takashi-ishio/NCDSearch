package ncdsearch.comparison.ncd;

import java.util.Arrays;

import com.github.luben.zstd.Zstd;

public class ZstdStrategy implements ICompressionStrategy {

	public ZstdStrategy() {
	}
	
	@Override
	public long getDataSize(byte[] buf, int start, int length) {
		if (start == 0 && buf.length == length) {
			return Zstd.compress(buf).length;
		} else {
			return Zstd.compress(Arrays.copyOfRange(buf, start, start+length)).length;
		}
	}
	
	@Override
	public void close() {
	}
}
