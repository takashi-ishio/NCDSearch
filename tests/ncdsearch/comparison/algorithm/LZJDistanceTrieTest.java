package ncdsearch.comparison.algorithm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

	/**
	 * Compare the results with LZJDistance2022 (strict mode) using source files in this repository.
	 */
	@Test
	public void testSameResultAsLZJDistance2022() throws IOException {
		List<TokenSequence> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(Paths.get("src", "ncdsearch"))) {
			for (Path p: paths.filter(p -> p.toString().endsWith(".java")).sorted().limit(20).collect(Collectors.toList())) {
				files.add(tokenize(Files.readAllBytes(p)));
			}
		}
		Assert.assertFalse(files.isEmpty());

		Random random = new Random(0);
		double[] thresholds = { 0.3, 0.5, 0.7 };
		for (int i=0; i<20; i++) {
			// Extract a query from a file
			TokenSequence source = files.get(random.nextInt(files.size()));
			int length = 5 + random.nextInt(40);
			if (source.size() <= length) continue;
			int start = random.nextInt(source.size() - length);
			TokenSequence query = source.substring(start, start + length);

			LZJDistanceTrie trie = new LZJDistanceTrie(query);
			LZJDistance2022 strict = new LZJDistance2022(query, true);
			int windowSize = (int)Math.ceil(query.size() * 1.2);
			for (double threshold: thresholds) {
				for (TokenSequence file: files) {
					for (int pos: file.getLineHeadTokenPositions()) {
						double expected = strict.findBestMatch(file, pos, pos + windowSize, threshold);
						double actual = trie.findBestMatch(file, pos, pos + windowSize, threshold);
						// Both implementations may stop at different positions if no match is found
						Assert.assertEquals(expected <= threshold, actual <= threshold);
						if (expected <= threshold) {
							Assert.assertEquals(expected, actual, 0.0);
							Assert.assertEquals(strict.getBestWindowSize(), trie.getBestWindowSize());
						}
					}
				}
			}
			trie.close();
			strict.close();
		}
	}
}
