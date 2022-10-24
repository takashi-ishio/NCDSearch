package ncdsearch.comparison.algorithm;

import java.io.IOException;

import gnu.trove.list.array.TLongArrayList;
import ncdsearch.SearchConfiguration;
import ncdsearch.comparison.TokenSequence;
import ncdsearch.files.IFile;
import ncdsearch.files.IFiles;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;

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
	
	/**
	 * Apply only PredictionFilter for files.
	 * @param args
	 */
	public static void main(String[] args) {
		SearchConfiguration config = new SearchConfiguration(args);
		PredictionFilter prefilter = config.getPrefilter();
		try (IFiles files = config.getFiles()) {
			for (IFile f = files.next(); f != null; f = files.next()) {
				try {
					TokenReader reader = TokenReaderFactory.create(config.getQueryLanguage(), f.read(), config.getSourceCharset());
					TokenSequence fileTokens = new TokenSequence(reader, config.useNormalization(), config.useSeparator());
					if (prefilter == null || prefilter.shouldSearch(fileTokens)) {
						System.out.println(f.getPath());
					}
				} catch (IOException e) {
				}
			}
		}
	}

}
