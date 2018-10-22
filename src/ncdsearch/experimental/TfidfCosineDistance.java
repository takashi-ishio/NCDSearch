package ncdsearch.experimental;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import ncdsearch.ICodeDistanceStrategy;
import ncdsearch.TokenSequence;
import sarf.lexer.DirectoryScan;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;

public class TfidfCosineDistance implements ICodeDistanceStrategy {

	private static TObjectIntHashMap<String> dfMap;
	private static TFIDFVector queryTFIDF;
	
	
	private static synchronized void computeDocumentFrequency(ArrayList<String> sourceDirs, FileType queryFileType, TokenSequence query) {
		dfMap = new TObjectIntHashMap<>();
		for (String dir: sourceDirs) {
			DirectoryScan.scan(new File(dir), new DirectoryScan.Action() {
				@Override
				public void process(File f) {
					FileType type = TokenReaderFactory.getFileType(f.getAbsolutePath());
					if (type == queryFileType) {
						try {
							HashSet<String> tokens = new HashSet<>(65536);
							TokenReader r = TokenReaderFactory.create(type, Files.readAllBytes(f.toPath()));
							while (r.next()) {
								tokens.add(r.getToken());
							}
							for (String t: tokens) {
								dfMap.adjustOrPutValue(t, 1, 1);
							}
						} catch (IOException e) {
						}
					}
				}
			});
		}
		queryTFIDF = new TFIDFVector(query);
	}
	
	/**
	 * Build IDF from source files   
	 */
	public TfidfCosineDistance(ArrayList<String> sourceDirs, FileType queryFileType, TokenSequence query) {
		computeDocumentFrequency(sourceDirs, queryFileType, query);
	}
	
	/**
	 * Make a tf-idf query using pre-computed IDF 
	 * @param dfMap maps a token to its document frequency 
	 * @param query
	 */
	protected TfidfCosineDistance(TObjectIntHashMap<String> dfMap, TokenSequence query) {
		TfidfCosineDistance.dfMap = dfMap;
		queryTFIDF = new TFIDFVector(query);
	}
	
	
	@Override
	public double computeDistance(TokenSequence code) {
		TFIDFVector codeTFIDF = new TFIDFVector(code);
		return 1 - queryTFIDF.getCosineSimilarity(codeTFIDF);
	}
	
	@Override
	public void close() {
	}
	
	private static class TFIDFVector {

		private TObjectDoubleHashMap<String> vec;
		private double absolute;
		
		public TFIDFVector(TokenSequence tokens) {
			vec = new TObjectDoubleHashMap<>();
			for (int i=0; i<tokens.size(); i++) {
				vec.adjustOrPutValue(tokens.getToken(i), 1, 1);
			}
			double squareSum = 0;
			for (String t: vec.keySet()) {
				int df = dfMap.get(t);
				if (df > 0) {
					double tfidf = vec.get(t) * 1.0 / dfMap.get(t);
					vec.put(t, tfidf);
					squareSum += tfidf * tfidf; 
				} else {
					vec.remove(t);
				}
			}
			absolute = Math.sqrt(squareSum);
		}
		
		public double getCosineSimilarity(TFIDFVector another) {
			double sum = 0;
			for (String t: vec.keySet()) {
				double d = another.vec.get(t);
				sum += vec.get(t) * d;
			}
			return sum / (this.absolute * another.absolute);
		}
		
	}
	

}
