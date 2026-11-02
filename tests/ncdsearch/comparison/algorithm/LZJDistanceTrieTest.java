package ncdsearch.comparison.algorithm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import ncdsearch.comparison.TokenSequence;
import sarf.lexer.FileType;
import sarf.lexer.TokenReaderFactory;

public class LZJDistanceTrieTest {

	private static TokenSequence tokenize(byte[] content) {
		return new TokenSequence(TokenReaderFactory.create(FileType.JAVA, content), false);
	}

	@Test
	public void testComputeDistance() {
		TokenSequence query = tokenize("if (x == null) return 0;".getBytes());
		LZJDistanceTrie trie = new LZJDistanceTrie(query);
		LZJDistance2022 strict = new LZJDistance2022(query, true);
		Assert.assertEquals(0.0, trie.computeDistance(query), 0.0);

		String[] targets = { "if (y == null) return 1;", "while (x != null) x = x.next;", "abc", "" };
		for (String t: targets) {
			TokenSequence target = tokenize(t.getBytes());
			Assert.assertEquals(t, strict.computeDistance(target), trie.computeDistance(target), 0.0);
		}
		trie.close();
		strict.close();
	}

	private static List<TokenSequence> loadFiles() throws IOException {
		List<TokenSequence> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(Paths.get("src", "ncdsearch"))) {
			for (Path p: paths.filter(p -> p.toString().endsWith(".java")).sorted().limit(20).collect(Collectors.toList())) {
				files.add(tokenize(Files.readAllBytes(p)));
			}
		}
		Assert.assertFalse(files.isEmpty());
		return files;
	}

	/**
	 * Extract a query from a file
	 */
	private static TokenSequence randomQuery(List<TokenSequence> files, Random random) {
		while (true) {
			TokenSequence source = files.get(random.nextInt(files.size()));
			int length = 5 + random.nextInt(40);
			if (source.size() > length) {
				int start = random.nextInt(source.size() - length);
				return source.substring(start, start + length);
			}
		}
	}

	/**
	 * Check whether LZJDistanceTrie reports the same result as LZJDistance2022 (strict mode).
	 * @return true if the window is similar to the query
	 */
	private static boolean assertSameResult(LZJDistance2022 strict, LZJDistanceTrie trie, TokenSequence file, int pos, int windowSize, double threshold) {
		double expected = strict.findBestMatch(file, pos, pos + windowSize, threshold);
		double actual = trie.findBestMatch(file, pos, pos + windowSize, threshold);
		// Both implementations may stop at different positions if no match is found
		Assert.assertEquals(expected <= threshold, actual <= threshold);
		if (expected <= threshold) {
			Assert.assertEquals(expected, actual, 0.0);
			Assert.assertEquals(strict.getBestWindowSize(), trie.getBestWindowSize());
			return true;
		}
		return false;
	}

	/**
	 * Compare the results with LZJDistance2022 (strict mode) using source files in this repository.
	 */
	@Test
	public void testSameResultAsLZJDistance2022() throws IOException {
		List<TokenSequence> files = loadFiles();
		Random random = new Random(0);
		double[] thresholds = { 0.3, 0.5, 0.7 };
		int matches = 0;
		for (int i=0; i<20; i++) {
			TokenSequence query = randomQuery(files, random);
			LZJDistanceTrie trie = new LZJDistanceTrie(query);
			LZJDistance2022 strict = new LZJDistance2022(query, true);
			int windowSize = (int)Math.ceil(query.size() * 1.2);
			for (double threshold: thresholds) {
				for (TokenSequence file: files) {
					for (int pos: file.getLineHeadTokenPositions()) {
						if (assertSameResult(strict, trie, file, pos, windowSize, threshold)) {
							matches++;
						}
					}
				}
			}
			trie.close();
			strict.close();
		}
		Assert.assertTrue(matches > 0);
	}

	/**
	 * Windows are compared in a random order with various sizes 
	 * so that the filter of LZJDistanceTrie must reset its state.
	 */
	@Test
	public void testRandomOrderWindows() throws IOException {
		List<TokenSequence> files = loadFiles();
		Random random = new Random(1);
		int matches = 0;
		for (int i=0; i<10; i++) {
			TokenSequence query = randomQuery(files, random);
			LZJDistanceTrie trie = new LZJDistanceTrie(query);
			LZJDistance2022 strict = new LZJDistance2022(query, true);
			
			// Every token position in every file, including the query location
			List<int[]> windows = new ArrayList<>();
			for (int f=0; f<files.size(); f++) {
				for (int pos=0; pos<files.get(f).size(); pos++) {
					windows.add(new int[] { f, pos });
				}
			}
			Collections.shuffle(windows, random);
			for (int[] w: windows.subList(0, Math.min(windows.size(), 20000))) {
				int windowSize = 1 + random.nextInt(query.size() * 2);
				double threshold = 0.2 + random.nextDouble() * 0.6;
				if (assertSameResult(strict, trie, files.get(w[0]), w[1], windowSize, threshold)) {
					matches++;
				}
			}
			// Scan a file token by token as -full option does
			TokenSequence file = files.get(random.nextInt(files.size()));
			int windowSize = (int)Math.ceil(query.size() * 1.2);
			for (int pos: file.getFullPositions(windowSize)) {
				if (assertSameResult(strict, trie, file, pos, windowSize, 0.5)) {
					matches++;
				}
			}
			trie.close();
			strict.close();
		}
		Assert.assertTrue(matches > 0);
	}
}
