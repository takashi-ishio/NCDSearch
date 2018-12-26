package sarf.lexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import ncdsearch.normalizer.CPP14Normalizer;
import sarf.lexer.lang.CPP14Lexer;
import sarf.lexer.lang.CSharpLexer;
import sarf.lexer.lang.ECMAScriptLexer;
import sarf.lexer.lang.Java8Lexer;
import sarf.lexer.lang.Python3Lexer;

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
		
		filetype.put("ccfx", FileType.CCFINDERX);
		filetype.put("ccfinderx", FileType.CCFINDERX);
		filetype.put("ccfxprep", FileType.CCFINDERX);

		filetype.put("py", FileType.PYTHON);

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
	private static CharStream createStream(byte[] buf, Charset charset) throws IOException {
		if (charset == StandardCharsets.UTF_8) {
			// Remove UTF-8 and UTF-16 BOM if exists to avoid an accidental token of BOM
			if (buf.length >= 3 && 
				buf[0] == (byte)0xEF && buf[1] == (byte)0xBB && buf[2] == (byte)0xBF) {
				return CharStreams.fromStream(new ByteArrayInputStream(buf, 3, buf.length-3));
			} else if (buf.length >= 2 && buf[0] == (byte)0xFE && buf[1] == (byte)0xFF) {
				return CharStreams.fromStream(new ByteArrayInputStream(buf, 2, buf.length-2));
			} else if (buf.length >= 2 && buf[0] == (byte)0xFF && buf[1] == (byte)0xFE) {
				return CharStreams.fromStream(new ByteArrayInputStream(buf, 2, buf.length-2));
			} else {
				return CharStreams.fromStream(new ByteArrayInputStream(buf));
			}
		} else {
			return CharStreams.fromStream(new ByteArrayInputStream(buf), charset);
		}
	}

	/**
	 * Create a TokenReader to read source code tokens.
	 * This method assumes the code as UTF-8.
	 */
	public static TokenReader create(FileType filetype, byte[] buf) {
		return create(filetype, buf, StandardCharsets.UTF_8);
	}

	/**
	 * Create a TokenReader to read source code tokens.
	 * @param filetype specifies a file type. It can be obtained by TokenReaderFactory#getFileType. 
	 * @param buf source code content. 
	 * @return a token reader.
	 */
	public static TokenReader create(FileType filetype, byte[] buf, Charset charset) {
		try {
			switch (filetype) {
			case CPP:
				LexerTokenReader r = new LexerTokenReader(filetype, new CPP14Lexer(createStream(buf, charset)));
				r.setNormalizer(new CPP14Normalizer());
				return r;
	
			case JAVA:
				return new LexerTokenReader(filetype, new Java8Lexer(createStream(buf, charset)));

			case ECMASCRIPT:
				return new LexerTokenReader(filetype, new ECMAScriptLexer(createStream(buf, charset)));
				
			case CSHARP:
				return new LexerTokenReader(filetype, new CSharpLexer(createStream(buf, charset)));

			case PYTHON:
				return new LexerTokenReader(filetype, new Python3Lexer(createStream(buf, charset)));

			case CCFINDERX:
				return new CCFinderXLexer(buf, charset);

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
				LexerTokenReader r = new LexerTokenReader(filetype, new CPP14Lexer(CharStreams.fromReader(reader)));
				r.setNormalizer(new CPP14Normalizer());
				return r;
	
			case JAVA:
				return new LexerTokenReader(filetype, new Java8Lexer(CharStreams.fromReader(reader)));

			case ECMASCRIPT:
				return new LexerTokenReader(filetype, new ECMAScriptLexer(CharStreams.fromReader(reader)));

			case CSHARP:
				return new LexerTokenReader(filetype, new CSharpLexer(CharStreams.fromReader(reader)));

			case PYTHON:
				return new LexerTokenReader(filetype, new Python3Lexer(CharStreams.fromReader(reader)));
				
			case CCFINDERX:
				return new CCFinderXLexer(reader);

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
