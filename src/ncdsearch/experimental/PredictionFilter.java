package ncdsearch.experimental;

import gnu.trove.list.array.TLongArrayList;
import ncdsearch.TokenSequence;

public class PredictionFilter {

	private TLongArrayList queryNgrams;
	
	public PredictionFilter(String key) {
	}
	
	public void setQuery(TokenSequence query) {
		queryNgrams = NgramDistance.createNgram(query.toByteArray(), 5);
	}
	
	private static final int N = 5; 
	public boolean shouldSearch(TokenSequence file) {
		try {
			byte[] buf = file.toByteArray();
			if (buf.length >= N) {
				TLongArrayList fileNgrams = NgramDistance.createNgram(buf, N);
				int intersection = NgramDistance.computeIntersection(queryNgrams, fileNgrams);
				double overlap = intersection * 1.0 / queryNgrams.size();
				return overlap >= 0.5;
			} else {
				return false;
			}
		} catch (Throwable t) {
			return true;
		}
	}

}
