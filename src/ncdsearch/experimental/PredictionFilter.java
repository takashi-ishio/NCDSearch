package ncdsearch.experimental;

import gnu.trove.list.array.TLongArrayList;
import ncdsearch.TokenSequence;

public class PredictionFilter {

	private TLongArrayList queryNgrams;
	private int N;
	private double threshold;
	
	public PredictionFilter(String key) {
		// Set default values
		N = 5;
		threshold = 0.5;
		// Try to parse a given parameter
		try {
			String[] tokens = key.split(",");
			if (tokens.length > 0) {
				int param = Integer.parseInt(tokens[0]);
				if (0 < param && param < 8) {
					 N = param;
				}

				if (tokens.length > 1) {
					double th = Double.parseDouble(tokens[1]);
					if (0 <= th && th <= 1) {
						 threshold = th;
					}
				}
			}
		} catch (NumberFormatException e) {
		}
	}
	
	public void setQuery(TokenSequence query) {
		queryNgrams = NgramDistance.createNgram(query.toByteArray(), N);
	}
	
	public boolean shouldSearch(TokenSequence file) {
		try {
			byte[] buf = file.toByteArray();
			if (buf.length >= N) {
				TLongArrayList fileNgrams = NgramDistance.createNgram(buf, N);
				int intersection = NgramDistance.computeIntersection(queryNgrams, fileNgrams);
				double overlap = intersection * 1.0 / queryNgrams.size();
				return overlap >= threshold;
			} else {
				return false;
			}
		} catch (Throwable t) {
			return true;
		}
	}

}
