package ncdsearch.normalizer;

import org.antlr.v4.runtime.Token;

public interface Normalizer {

	public String normalize(Token t);
}
