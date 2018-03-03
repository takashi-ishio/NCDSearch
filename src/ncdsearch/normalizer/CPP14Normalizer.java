package ncdsearch.normalizer;


import org.antlr.v4.runtime.Token;

import sarf.lexer.lang.CPP14Lexer;

public class CPP14Normalizer implements Normalizer {

	@Override
	public String normalize(Token t) {
		switch (t.getType()) {
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
		return t.getText();
	}
}
