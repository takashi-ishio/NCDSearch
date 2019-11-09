package ncdsearch.eval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import ncdsearch.ICodeDistanceStrategy;
import ncdsearch.NormalizedCompressionDistance;
import ncdsearch.SearchConfiguration;
import ncdsearch.SearchMain;
import ncdsearch.TokenSequence;
import ncdsearch.experimental.LZJDistance;
import ncdsearch.experimental.NormalizedTokenLevenshteinDistance;
import ncdsearch.ncd.Compressor;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;

/**
 * Compare given two files and report their similarity. 
 * @author ishio
 */
public class FileComparison {

	public static void main(String[] args) {
		int idx = 0;
		boolean searchBest = false;
		boolean useNormalizedTokenLD = false;
		boolean useLZJD = false;
		ArrayList<File> files = new ArrayList<>();
		Compressor compressor = null;
		while (idx < args.length) {
			if (args[idx].equals("-best")) {
				searchBest = true;
				idx++;
			} else if (args[idx].equals(SearchConfiguration.ARG_COMPRESSOR)) {
				idx++;
				if (idx < args.length) {
					compressor = Compressor.valueOf(args[idx++].toUpperCase());
				}
			} else if (args[idx].equals("-ntld")) {
				useNormalizedTokenLD = true;
				idx++;
			} else if (args[idx].equals("-lzjd")) {
				useLZJD = true;
				idx++;
			} else {
				files.add(new File(args[idx++]));
			}
		}

		if (files.size() != 2 || 
			!files.get(0).isFile() ||
			!files.get(0).canRead() ||
			!files.get(1).isFile() ||
			!files.get(1).canRead()) {
			System.err.println("Usage: FileComparison [-best] [-compressor (ZIP|XZ|ZSTD)] [-ntld] queryfile comparedfile");
			return;
		}

		File query = files.get(0);
		File target = files.get(1);
		if (searchBest) {
			System.out.println(searchBestMatch(query, target, compressor));
		} else {
			TokenSequence tokens1 = read(query);
			TokenSequence tokens2 = read(target);
			
			ICodeDistanceStrategy strategy = null;
			if (useNormalizedTokenLD) {
				strategy = new NormalizedTokenLevenshteinDistance(tokens1);
			} else if (useLZJD) {
				strategy = new LZJDistance(tokens1);
			} else {
				strategy = new NormalizedCompressionDistance(tokens1, Compressor.createInstance(compressor));
			}
			
			double distance = strategy.computeDistance(tokens2);
			strategy.close();
			System.out.println(distance);
		}
	}
	
	public static double searchBestMatch(File query, File target, Compressor c) {
		TokenSequence queryTokens = read(query);
		TokenSequence tokens = read(target);

		try (NormalizedCompressionDistance ncd = new NormalizedCompressionDistance(queryTokens, Compressor.createInstance(c))) {
			double min = Double.MAX_VALUE;
			for (int i=0; i<tokens.size(); i++) {
				for (int j=i+1; j<=tokens.size(); j++) {
					TokenSequence sub = tokens.substring(i, j);
					double value = ncd.computeDistance(sub);
					if (value < min) min = value;
				}
			}
			return min;
		}
	}
	
	private static TokenSequence read(File f) {
		try {
			FileType t = TokenReaderFactory.getFileType(f.getAbsolutePath());
			if (t != FileType.UNSUPPORTED) {
				TokenReader reader = TokenReaderFactory.create(t, Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
				return new TokenSequence(reader, false);
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
