package ncdsearch.normalizer;


import sarf.lexer.TokenReader;
import sarf.lexer.lang.CPP14Lexer;

public class CPP14Normalizer extends DefaultNormalizer {

	public CPP14Normalizer(TokenReader r) {
		super(r);
	}
	
	@Override
	public String getToken() {
		switch (getTokenType()) {
		case CPP14Lexer.Identifier:
		case CPP14Lexer.Binaryliteral:
		case CPP14Lexer.Characterliteral:
		case CPP14Lexer.Decimalliteral:
		case CPP14Lexer.Floatingliteral:
		case CPP14Lexer.Hexadecimalliteral:
		case CPP14Lexer.Integerliteral:
		case CPP14Lexer.Octalliteral:
		case CPP14Lexer.Stringliteral:
		case CPP14Lexer.Userdefinedcharacterliteral:
		case CPP14Lexer.Userdefinedfloatingliteral:
		case CPP14Lexer.Userdefinedintegerliteral:
		case CPP14Lexer.Userdefinedstringliteral:
			return "$p";
		}
		return super.getToken();
	}
}
