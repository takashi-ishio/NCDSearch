lexer grammar GenericLexer;

Word: [a-zA-Z0-9_]+;

Symbol: ~[a-zA-Z0-9_ \t\r\n];

WS: [ \t\r\n\f]+ -> skip;

ErrorCharacter: .;
