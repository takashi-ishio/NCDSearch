package ncdsearch.comparison.algorithm;

import java.util.Arrays;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import ncdsearch.comparison.IVariableWindowStrategy;
import ncdsearch.comparison.TokenSequence;

/**
 * A faster implementation of LZJDistance2022 (strict mode).
 *
 * An LZSet is prefix-closed: when Algorithm 1 checks a phrase B[start:end],
 * its prefix B[start:end-1] is always included in the set.
 * Hence, each phrase is represented by a pair of (its prefix, the last byte),
 * i.e., a node of an LZ78 trie.
 * This class keeps the LZSet of a query as a trie with a dense transition table,
 * so that the membership of a phrase is checked in O(1) per byte
 * without hashing the whole phrase.
 * The result is the same as LZJDistance2022 with strict comparison.
 *
 * In addition, findBestMatch skips a window that cannot satisfy a threshold.
 * Every common element of LZSet(q) and LZSet(w) is a query phrase occurring in w as a substring.
 * Since LZJD(q, w) <= threshold requires |LZSet(q) ∩ LZSet(w)| >= (1 - threshold) * |LZSet(q)|,
 * a window including fewer query phrases is never reported.
 * The number of query phrases in a window is incrementally updated
 * when windows move forward in a file.
 */
public class LZJDistanceTrie implements IVariableWindowStrategy {

	/**
	 * The node representing an empty phrase in tries.
	 */
	private static final int EMPTY_PHRASE = 0;

	/**
	 * A map from a byte to a column of the transition table, or -1 if the byte does not appear in the query.
	 */
	private int[] column;

	/**
	 * The number of distinct bytes in the query.
	 */
	private int alphabetSize;

	/**
	 * The transition table of the query trie.
	 * qnext[node * alphabetSize + column[b]] is the child node of the node for byte b, or 0 if it does not exist.
	 * Nodes 1..querySetSize represent the phrases in the LZSet of the query.
	 */
	private int[] qnext;

	/**
	 * The size of the LZSet of the query, i.e., the number of non-root nodes in the query trie.
	 */
	private int querySetSize;

	/**
	 * The LZSet of a target, which is cleared for each comparison.
	 * targetQueryPhrases[node] is true if the query phrase is included in the set.
	 * Other phrases are kept as a trie in targetOtherPhrases;
	 * a key is an edge (parent node, byte) and a value is a node larger than querySetSize.
	 */
	private boolean[] targetQueryPhrases;
	private Long2IntOpenHashMap targetOtherPhrases = new Long2IntOpenHashMap();

	/**
	 * The number of query phrases included in the LZSet of a target.
	 */
	private int intersection;

	private int bestWindowSize;

	/**
	 * A byte array of a file analyzed by the filter.
	 * The filter keeps the state for a window buf[filterStart, filterEnd)
	 * so that the state is incrementally updated for the next window.
	 */
	private byte[] filterBuf;
	private int filterStart;
	private int filterEnd;

	/**
	 * occurrences[node] is the number of occurrences of the query phrase in the filter window.
	 */
	private int[] occurrences;

	/**
	 * The number of query phrases occurring at least once in the filter window.
	 */
	private int distinctPhrases;

	/**
	 * Walks on the query trie that start in the filter window and reach its end.
	 * They are extended when the window is extended.
	 * The number of walks is at most the length of the longest query phrase.
	 */
	private int[] activeStarts;
	private int[] activeNodes;
	private int activeCount;

	/**
	 * Create a strategy instance with a query code fragment.
	 * @param query is a query code fragment.
	 */
	public LZJDistanceTrie(TokenSequence query) {
		// Build a query trie using Algorithm 1
		byte[] b = query.toByteArray();
		Long2IntOpenHashMap edges = new Long2IntOpenHashMap();
		int node = EMPTY_PHRASE;
		int count = 0;
		int phraseLength = 0;
		int maxPhraseLength = 0;
		for (int i=0; i<b.length; i++) {
			long key = edgeKey(node, b[i] & 0xff);
			int child = edges.get(key);
			phraseLength++;
			if (child != 0) {
				node = child;
			} else {
				count++;
				edges.put(key, count);
				node = EMPTY_PHRASE;
				maxPhraseLength = Math.max(maxPhraseLength, phraseLength);
				phraseLength = 0;
			}
		}
		querySetSize = count;

		// Use only bytes in the query so that the table is small enough for CPU cache
		column = new int[256];
		Arrays.fill(column, -1);
		for (int i=0; i<b.length; i++) {
			if (column[b[i] & 0xff] < 0) {
				column[b[i] & 0xff] = alphabetSize++;
			}
		}
		qnext = new int[(count + 1) * Math.max(1, alphabetSize)];
		for (Long2IntMap.Entry e: edges.long2IntEntrySet()) {
			long key = e.getLongKey();
			qnext[(int)(key >>> 8) * alphabetSize + column[(int)(key & 0xff)]] = e.getIntValue();
		}
		targetQueryPhrases = new boolean[count + 1];

		occurrences = new int[count + 1];
		activeStarts = new int[maxPhraseLength + 1];
		activeNodes = new int[maxPhraseLength + 1];
	}

	private static long edgeKey(int parent, int b) {
		return ((long)parent << 8) | b;
	}

	/**
	 * @return the child of a query trie node for byte b, or 0 if it does not exist.
	 */
	private int child(int node, int b) {
		int c = column[b];
		return c < 0 ? 0 : qnext[node * alphabetSize + c];
	}

	/**
	 * Count query phrases occurring in buf[start, end).
	 * The state is incrementally updated if the window moves forward from the last call.
	 * @return the number of distinct query phrases in the window.
	 */
	private int countQueryPhrases(byte[] buf, int start, int end) {
		if (buf != filterBuf || start < filterStart || end < filterEnd || start >= filterEnd) {
			// Restart counting from an empty window
			filterBuf = buf;
			filterStart = start;
			filterEnd = start;
			Arrays.fill(occurrences, 0);
			distinctPhrases = 0;
			activeCount = 0;
		}

		if (filterStart < start) {
			// Remove occurrences starting before the window
			for (int i=filterStart; i<start; i++) {
				int node = 0;
				for (int j=i; j<filterEnd; j++) {
					node = child(node, buf[j] & 0xff);
					if (node == 0) break;
					if (--occurrences[node] == 0) {
						distinctPhrases--;
					}
				}
			}
			int k = 0;
			for (int a=0; a<activeCount; a++) {
				if (activeStarts[a] >= start) {
					activeStarts[k] = activeStarts[a];
					activeNodes[k] = activeNodes[a];
					k++;
				}
			}
			activeCount = k;
			filterStart = start;
		}

		// Add occurrences ending in the extended part of the window
		for (int j=filterEnd; j<end; j++) {
			int b = buf[j] & 0xff;
			int k = 0;
			for (int a=0; a<activeCount; a++) {
				int node = child(activeNodes[a], b);
				if (node != 0) {
					if (occurrences[node]++ == 0) {
						distinctPhrases++;
					}
					activeStarts[k] = activeStarts[a];
					activeNodes[k] = node;
					k++;
				}
			}
			int node = child(0, b);
			if (node != 0) {
				if (occurrences[node]++ == 0) {
					distinctPhrases++;
				}
				activeStarts[k] = j;
				activeNodes[k] = node;
				k++;
			}
			activeCount = k;
		}
		filterEnd = end;
		return distinctPhrases;
	}

	/**
	 * Start a new LZSet for a target.
	 */
	private void clearTargetSet() {
		Arrays.fill(targetQueryPhrases, false);
		targetOtherPhrases.clear();
		intersection = 0;
	}

	/**
	 * Add phrases in buf[from, to) to the LZSet of a target (Algorithm 1).
	 * @param phrase is the node of an incomplete phrase continued from the previous bytes.
	 * @return the node of an incomplete phrase at the end of the bytes.
	 * The method returns EMPTY_PHRASE if the last byte completed a new phrase.
	 */
	private int scan(byte[] buf, int from, int to, int phrase) {
		for (int i=from; i<to; i++) {
			int b = buf[i] & 0xff;
			if (phrase <= querySetSize) {
				// The current phrase is in the query trie
				int q = child(phrase, b);
				if (q != 0) {
					if (targetQueryPhrases[q]) {
						phrase = q;
					} else {
						// A new element that is also included in the query LZSet
						targetQueryPhrases[q] = true;
						intersection++;
						phrase = EMPTY_PHRASE;
					}
					continue;
				}
			}
			// The phrase is not in the query LZSet
			long key = edgeKey(phrase, b);
			int other = targetOtherPhrases.get(key);
			if (other != 0) {
				phrase = other;
			} else {
				targetOtherPhrases.put(key, querySetSize + 1 + targetOtherPhrases.size());
				phrase = EMPTY_PHRASE;
			}
		}
		return phrase;
	}

	/**
	 * Compare a given code fragment with the query and
	 * find the best LZJD value and its window size
	 * @param code is an entire file including the fragment to be compared.
	 * @param startPos specifies the first token index of the fragment to be compared
	 * @param endPos specifies the last token index of the fragment to be compared
	 * @param threshold specifies the maximum distance of LZJD
	 * @return the best LZJD value
	 */
	@Override
	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold) {
		if (endPos > code.size()) endPos = code.size();

		double bestLZJD = Double.MAX_VALUE;
		bestWindowSize = 0;

		byte[] buf = code.toByteArray();
		int windowSize = endPos - startPos;
		if (windowSize <= 0) {
			return bestLZJD;
		}

		// Skip the window if it does not include enough query phrases.
		// A small margin avoids skipping a window due to a rounding error.
		double requiredPhrases = (1 - threshold) * querySetSize - 1e-9;
		if (requiredPhrases > 0 &&
			countQueryPhrases(buf, code.getBytePosition(startPos), code.getBytePosition(endPos)) < requiredPhrases) {
			return bestLZJD;
		}

		int allowedMaxUnmatched = 1 + (int)(threshold * querySetSize / (1 - threshold));

		clearTargetSet();
		int phrase = EMPTY_PHRASE;
		int pos = code.getBytePosition(startPos);
		// For each token position, update LZSet and LZJD
		for (int t=1; t<=windowSize; t++) {
			int nextPos = code.getBytePosition(startPos + t);
			phrase = scan(buf, pos, nextPos, phrase);
			pos = nextPos;

			int unmatched = targetOtherPhrases.size();
			int unionSize = querySetSize + unmatched;
			double lzjd = (double)(unionSize - intersection) / unionSize;
			if (lzjd < bestLZJD) {
				bestLZJD = lzjd;
				bestWindowSize = t;
			}

			// Terminate the loop early, if no chance
			if (unmatched > allowedMaxUnmatched) {
				break;
			}
			// Even if all the remaining query elements are matched,
			// LZJD cannot be smaller than the current best
			if ((double)unmatched / unionSize >= bestLZJD) {
				break;
			}
		}
		return bestLZJD;
	}

	/**
	 * @return the length (number of tokens) of the best substring
	 * identified by the last call of findBestMatch
	 */
	@Override
	public int getBestWindowSize() {
		return bestWindowSize;
	}

	/**
	 * This method directly calculates LZJD for a target code fragment.
	 * @param code is a target code fragment
	 * @return the LZJD value between the query and the target code fragment
	 */
	@Override
	public double computeDistance(TokenSequence code) {
		byte[] buf = code.toByteArray();
		clearTargetSet();
		scan(buf, 0, buf.length, EMPTY_PHRASE);
		int unionSize = querySetSize + targetOtherPhrases.size();
		double lzjd = (double)(unionSize - intersection) / unionSize;
		// Release a large table allocated for the entire code
		targetOtherPhrases.clear();
		targetOtherPhrases.trim();
		return lzjd;
	}

	@Override
	public void close() {
		// Release the reference to the last file
		filterBuf = null;
	}

}
