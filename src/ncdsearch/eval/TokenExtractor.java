package ncdsearch.eval;

import ncdsearch.SearchMain;
import ncdsearch.TokenSequence;

/**
 * Extract TokenSequence from files (for testing)
 */
public class TokenExtractor {

	public static void main(String[] args) {
		SearchMain main = new SearchMain(args);
		TokenSequence seq = main.getQueryTokens();
		if (seq != null) {
			for (int i=0; i<seq.size(); i++) {
				System.out.println(seq.getToken(i));
			}
		}
	}

}
