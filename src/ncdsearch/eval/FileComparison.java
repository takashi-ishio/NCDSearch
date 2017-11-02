package ncdsearch.eval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import ncdsearch.NormalizedCompressionDistance;
import ncdsearch.TokenSequence;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;

/**
 * Compare given two files and report their similarity. 
 * @author ishio
 */
public class FileComparison {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: FileComparison filename1 filename2");
			return;
		}
		TokenSequence tokens1 = read(args[0]);
		TokenSequence tokens2 = read(args[1]);

		try (NormalizedCompressionDistance ncd = new NormalizedCompressionDistance(tokens1)) {
			System.out.println(ncd.ncd(tokens2));
		}
	}
	
	private static TokenSequence read(String filename) {
		try {
			FileType t = TokenReaderFactory.getFileType(filename);
			if (t != FileType.UNSUPPORTED) {
				TokenReader reader = TokenReaderFactory.create(t, Files.readAllBytes(new File(filename).toPath()));
				return new TokenSequence(reader);
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
