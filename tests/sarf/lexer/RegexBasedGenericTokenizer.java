package sarf.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;


/**
 * A generic source code tokenizer for all programming languages.
 * This class simply splits a line into tokens using white spaces.
 * Each symbol other than alphabets and numbers are regarded as a token.
 * As the tokenizer use only simple rules, code comments and 
 * string literals are also split into tokens.
 * 
 * This class has been used for 
 */
public class RegexBasedGenericTokenizer implements TokenReader {

    /** 
     * Regex to match words, numbers, underscores or single non-alphanumeric characters
     * and a non-word except for white spaces 
     */
    private static Pattern pattern = Pattern.compile("[a-zA-Z0-9_]+|[^\\s\\w]");
	private List<Token> tokens;
	private int index = -1;
	
	/**
	 * The constructor extract tokens from a reader.
	 * @param r contains source code.
	 */
	public RegexBasedGenericTokenizer(Reader r) {
		tokens = tokenizeFile(r);
	}
	
    @Override
    public boolean next() {
    	if(tokens != null) {
    		index++;
    		if(index < tokens.size()) {
    			return true;
    		}
    	}
    	return false;
    }
    
    @Override
    public String getToken() {
    	if(index >= 0 && index < tokens.size()) {
    		Token currentToken = tokens.get(index);
    		return currentToken.value;
    	}
    	
    	return null;
    }
    
    /**
     * This tokenizer does not normalize identifiers.
     */
    @Override
    public String getNormalizedToken() {
    	return getToken();
    }
    
    @Override
    public int getLine() {
    	 if(index >= 0 && index < tokens.size()) {
    		 Token currentToken = tokens.get(index);
    		 return currentToken.line;
    	 }
    	return 0;
    }
    
    @Override
    public FileType getFileType() {
    	return FileType.GENERIC;
    }
    
    @Override
    public int getCharPositionInLine() {
    	 if(index >= 0 && index < tokens.size()) {
    		 Token currentToken = tokens.get(index);
    		 return currentToken.position;
    	 }
    	return 0;
    }
    
    /**
     * Extract tokens from a reader.
     * @param r specifies source code.
     * @return a list of tokens.
     */
    static List<Token> tokenizeFile(Reader r) {
        List<Token> tokens = new ArrayList<>();
        try (LineNumberReader lines = new LineNumberReader(r)) {
	        for (String line = lines.readLine(); line != null; line = lines.readLine()) {
	            tokens.addAll(tokenizeLine(line, lines.getLineNumber()));
	        }
        } catch (IOException e) {
        	throw new RuntimeException("An error occurred while reading a file.", e);
        }
        return tokens;
    }

    /**
     * Extract tokens from a line.
     * @param code is the content of the line.
     * @param lineNumber is the line number.
     * @return a list of tokens.
     */
    static List<Token> tokenizeLine(String code, int lineNumber) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            tokens.add(new Token(matcher.group(), lineNumber, matcher.start() + 1));
        }
        return tokens;
    }

    /**
     * An object representing a string and its position
     */
    static class Token {

    	String value;    	
        int line;
        int position;

        Token(String value, int line, int position) {
            this.value = value;
            this.line = line;
            this.position = position;
        }
    }

}

