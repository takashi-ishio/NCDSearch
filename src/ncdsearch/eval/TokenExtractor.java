package ncdsearch.eval;

import java.util.Arrays;

import ncdsearch.SearchConfiguration;
import ncdsearch.TokenSequence;

/**
 * Extract TokenSequence from files (for testing)
 */
public class TokenExtractor {

	public static void main(String[] args) {
		SearchConfiguration config = new SearchConfiguration(args);
		TokenSequence seq = config.getQueryTokens();
		if (seq != null) {
			for (int i=0; i<seq.size(); i++) {
				System.out.println(seq.getToken(i));
			}
			byte[] buf = seq.toByteArray();
			System.out.println(Arrays.toString(buf));
		}
	}

}
