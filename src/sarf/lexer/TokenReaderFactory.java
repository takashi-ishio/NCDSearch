package sarf.lexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import sarf.lexer.lang.CPP14Lexer;
import sarf.lexer.lang.CSharpLexer;
import sarf.lexer.lang.ECMAScriptLexer;
import sarf.lexer.lang.Java8Lexer;

public class TokenReaderFactory {
	
	private static HashMap<String, FileType> filetype;
	static {
		filetype = new HashMap<>(64);
		filetype.put("c", FileType.CPP);
		filetype.put("cc", FileType.CPP);
		filetype.put("cp", FileType.CPP);
		filetype.put("cpp", FileType.CPP);
		filetype.put("cx", FileType.CPP);
		filetype.put("cxx", FileType.CPP);
		filetype.put("c+", FileType.CPP);
		filetype.put("c++", FileType.CPP);
		filetype.put("h", FileType.CPP);
		filetype.put("hh", FileType.CPP);
		filetype.put("hxx", FileType.CPP);
		filetype.put("h+", FileType.CPP);
		filetype.put("h++", FileType.CPP);
		filetype.put("hp", FileType.CPP);
		filetype.put("hpp", FileType.CPP);

		filetype.put("java", FileType.JAVA);
		
		filetype.put("js", FileType.ECMASCRIPT);

		filetype.put("cs", FileType.CSHARP);
	}
	

	public static boolean isSupported(String filename) {
		return isSupported(getFileType(filename));
	}

	public static boolean isSupported(FileType filetype) {
		return filetype != FileType.UNSUPPORTED;
	}

	public static FileType getFileType(String filename) {
		// Remove directories 
		int index = filename.lastIndexOf("/");
		filename = filename.substring(index+1);
		
		if (filename.startsWith("._")) { // Mac OS's backup file
			return FileType.UNSUPPORTED;
		}
		
		index = filename.lastIndexOf('.');
		if (index < 0) {
			return FileType.UNSUPPORTED;
		}
		String ext = filename.substring(index + 1);
		FileType type = filetype.get(ext);
		if (type == null) {
			type = filetype.get(ext.toLowerCase());
		}
		if (type != null) {
			return type;
		}
		return FileType.UNSUPPORTED;
	}

	/**
	 * Create a stream for an ANTLR lexer.
	 * This method handles UTF-8/16 BOM.
	 * @param buf bytes be parsed.
	 * @return an instance of ANTLR CharStream.
	 * @throws IOException may be thrown if instantiation failed.
	 */
	private static CharStream createStream(byte[] buf) throws IOException {
		if (buf.length >= 3 && 
			buf[0] == (byte)0xEF && buf[1] == (byte)0xBB && buf[2] == (byte)0xBF) {
			return CharStreams.fromStream(new ByteArrayInputStream(buf, 3, buf.length-3));
		} else if (buf.length >= 2 && buf[0] == (byte)0xFE && buf[1] == (byte)0xFF) {
			return CharStreams.fromStream(new ByteArrayInputStream(buf, 2, buf.length-2), Charset.forName("UTF-16BE"));
		} else if (buf.length >= 2 && buf[0] == (byte)0xFF && buf[1] == (byte)0xFE) {
			return CharStreams.fromStream(new ByteArrayInputStream(buf, 2, buf.length-2), Charset.forName("UTF-16LE"));
		} else {
			return CharStreams.fromStream(new ByteArrayInputStream(buf));
		}
	}
	
	/**
	 * Create a TokenReader to read source code tokens.
	 * @param filetype specifies a file type. It can be obtained by TokenReaderFactory#getFileType. 
	 * @param buf source code content. 
	 * @return a token reader.
	 */
	public static TokenReader create(FileType filetype, byte[] buf) {
		try {
			switch (filetype) {
			case CPP:
				return new LexerTokenReader(filetype, new CPP14Lexer(createStream(buf)));
	
			case JAVA:
				return new LexerTokenReader(filetype, new Java8Lexer(createStream(buf)));

			case ECMASCRIPT:
				return new LexerTokenReader(filetype, new ECMAScriptLexer(createStream(buf)));
				
			case CSHARP:
				return new LexerTokenReader(filetype, new CSharpLexer(createStream(buf)));
				
			case UNSUPPORTED:
			default:
				return null;
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create a token reader reading source code from a given reader.
	 * @param filetype specifies a file type that can be obtained by TokenReaderFactory#getFileType. 
	 * @param reader is source code.  The object is automatically closed.  
	 * @return a token reader.
	 */
	public static TokenReader create(FileType filetype, Reader reader) {
		try {
			switch (filetype) {
			case CPP:
				return new LexerTokenReader(filetype, new CPP14Lexer(CharStreams.fromReader(reader)));
	
			case JAVA:
				return new LexerTokenReader(filetype, new Java8Lexer(CharStreams.fromReader(reader)));

			case ECMASCRIPT:
				return new LexerTokenReader(filetype, new ECMAScriptLexer(CharStreams.fromReader(reader)));

			case CSHARP:
				return new LexerTokenReader(filetype, new CSharpLexer(CharStreams.fromReader(reader)));

			case UNSUPPORTED:
			default:
				return null;
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
