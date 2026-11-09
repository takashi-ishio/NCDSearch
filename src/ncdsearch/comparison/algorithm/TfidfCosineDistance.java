package ncdsearch.comparison.algorithm;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ncdsearch.comparison.ICodeDistanceStrategy;
import ncdsearch.comparison.TokenSequence;
import ncdsearch.files.DirectoryScan;
import ncdsearch.files.IFile;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;

public class TfidfCosineDistance implements ICodeDistanceStrategy {

	private static Object2IntOpenHashMap<String> dfMap;
	private static TFIDFVector queryTFIDF;
	
	
	private static synchronized void computeDocumentFrequency(ArrayList<String> sourceDirs, FileType queryFileType, TokenSequence query, Charset charset) {
		if (dfMap == null) {			
			dfMap = new Object2IntOpenHashMap<>();
			try {
				DirectoryScan dir = new DirectoryScan(sourceDirs);
				for (IFile f=dir.next(); f != null; f=dir.next()) {
					FileType type = TokenReaderFactory.getFileType(f.getPath());
					if (type == queryFileType) {
						try {
							HashSet<String> tokens = new HashSet<>(65536);
							TokenReader r = TokenReaderFactory.create(type, f.read(), charset);
							while (r.next()) {
								tokens.add(r.getToken());
							}
							for (String t: tokens) {
								dfMap.addTo(t, 1);
							}
						} catch (IOException e) {
						}
					}
				}
				dir.close();
			} finally {
				// Initialize a query vector even if dfMap is not properly initialized
				queryTFIDF = new TFIDFVector(query);
			}
		}
	}
	
	/**
	 * Build IDF from source files   
	 */
	public TfidfCosineDistance(ArrayList<String> sourceDirs, FileType queryFileType, TokenSequence query, Charset charset) {
		computeDocumentFrequency(sourceDirs, queryFileType, query, charset);
	}
	
	/**
	 * Make a tf-idf query using pre-computed IDF 
	 * @param dfMap maps a token to its document frequency 
	 * @param query
	 */
	protected TfidfCosineDistance(Object2IntOpenHashMap<String> dfMap, TokenSequence query) {
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

		private Object2DoubleOpenHashMap<String> vec;
		private double absolute;
		
		public TFIDFVector(TokenSequence tokens) {
			vec = new Object2DoubleOpenHashMap<>();
			for (int i=0; i<tokens.size(); i++) {
				vec.addTo(tokens.getToken(i), 1);
			}
			// Translate tf vector to tf-idf vector
			double squareSum = 0;
			for (String t: vec.keySet()) {
				double tf = vec.getDouble(t);
				int df = dfMap.getInt(t);
				// assume IDF == 1 if the term does not appear in documents
				double tfidf = (df > 0) ? (tf / dfMap.getInt(t)) : tf; 
				vec.put(t, tfidf);
				squareSum += tfidf * tfidf; 
			}
			absolute = Math.sqrt(squareSum);
		}
		
		public double getCosineSimilarity(TFIDFVector another) {
			double sum = 0;
			for (String t: vec.keySet()) {
				double d = another.vec.getDouble(t);
				sum += vec.getDouble(t) * d;
			}
			return sum / (this.absolute * another.absolute);
		}
		
	}
	

}
