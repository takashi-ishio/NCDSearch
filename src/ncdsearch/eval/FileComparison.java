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
			System.err.println("Usage: FileComparison [-best] queryfile comparedfile");
			return;
		}
		if (args[0].equals("-best")) {
			System.out.println(searchBestMatch(args[1], args[2]));
		} else {
			System.out.println(compareSimply(args[0], args[1]));
		}
	}
	
	public static double compareSimply(String query, String target) {
		TokenSequence tokens1 = read(query);
		TokenSequence tokens2 = read(target);

		try (NormalizedCompressionDistance ncd = new NormalizedCompressionDistance(tokens1)) {
			return ncd.ncd(tokens2);
		}
	}
	
	public static double searchBestMatch(String query, String target) {
		TokenSequence queryTokens = read(query);
		TokenSequence tokens = read(target);

		try (NormalizedCompressionDistance ncd = new NormalizedCompressionDistance(queryTokens)) {
			double min = Double.MAX_VALUE;
			for (int i=0; i<tokens.size(); i++) {
				for (int j=i+1; j<=tokens.size(); j++) {
					TokenSequence sub = tokens.substring(i, j);
					double value = ncd.ncd(sub);
					if (value < min) min = value;
				}
			}
			return min;
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
