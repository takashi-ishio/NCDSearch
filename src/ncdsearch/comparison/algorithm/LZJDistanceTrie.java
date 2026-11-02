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
 * Phrases that are not in the query's LZSet are kept in a small hash table
 * whose entries are discarded in O(1) by incrementing an epoch counter.
 * The result is the same as LZJDistance2022 with strict comparison.
 */
public class LZJDistanceTrie implements IVariableWindowStrategy {

	private static final int NOT_FOUND = -1;

	private static final int INITIAL_LOCAL_CAPACITY = 256;

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
	 * The node 0 is the root representing an empty phrase.
	 */
	private int[] qnext;

	/**
	 * The size of the LZSet of the query, i.e., the number of non-root nodes in the query trie.
	 */
	private int querySetSize;

	/**
	 * stamp[node] == epoch represents that the query phrase is included in the LZSet of a target.
	 */
	private int[] stamp;

	/**
	 * This counter is incremented for each comparison to discard the previous target LZSet.
	 */
	private int epoch;

	/**
	 * An open addressing hash table for phrases that are not included in the query trie.
	 * A key is an edge (parent node, byte) and a value is the node ID of the phrase.
	 */
	private long[] localKeys;
	private int[] localValues;
	private int[] localStamps;
	private int localMask;
	private int localCount;

	/**
	 * The node representing the prefix of the current phrase.
	 */
	private int current;

	/**
	 * True if the current node is in the query trie.
	 */
	private boolean inQuery;

	/**
	 * A node ID for the next phrase that is not in the query trie.
	 * IDs larger than querySetSize are used so that they are distinguished from query nodes.
	 */
	private int nextLocalId;

	/**
	 * The size of the LZSet of a target.
	 */
	private int setSize;

	/**
	 * The number of elements included in both LZSets of the query and a target.
	 */
	private int intersection;

	private int bestWindowSize;

	/**
	 * Create a strategy instance with a query code fragment.
	 * @param query is a query code fragment.
	 */
	public LZJDistanceTrie(TokenSequence query) {
		// Build a query trie using Algorithm 1
		byte[] b = query.toByteArray();
		Long2IntOpenHashMap edges = new Long2IntOpenHashMap();
		int node = 0;
		int count = 0;
		for (int i=0; i<b.length; i++) {
			long key = edgeKey(node, b[i] & 0xff);
			int child = edges.get(key);
			if (child != 0) {
				node = child;
			} else {
				count++;
				edges.put(key, count);
				node = 0;
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
		stamp = new int[count + 1];
		allocateLocal(INITIAL_LOCAL_CAPACITY);
	}

	private static long edgeKey(int parent, int b) {
		return ((long)parent << 8) | b;
	}

	/**
	 * Start a new LZSet for a target.
	 */
	private void reset() {
		epoch++;
		if (epoch == 0) {
			// The counter overflowed; clear stamps to avoid wrong matches
			Arrays.fill(stamp, 0);
			Arrays.fill(localStamps, 0);
			epoch = 1;
		}
		current = 0;
		inQuery = true;
		nextLocalId = querySetSize + 1;
		setSize = 0;
		intersection = 0;
		localCount = 0;
	}

	/**
	 * Update the LZSet of a target by bytes buf[from..to-1].
	 */
	private void scan(byte[] buf, int from, int to) {
		for (int i=from; i<to; i++) {
			int b = buf[i] & 0xff;
			int c = column[b];
			if (inQuery && c >= 0) {
				int q = qnext[current * alphabetSize + c];
				if (q != 0) {
					if (stamp[q] == epoch) {
						current = q;
					} else {
						// A new element that is also included in the query LZSet
						stamp[q] = epoch;
						setSize++;
						intersection++;
						current = 0;
					}
					continue;
				}
			}
			// The phrase is not in the query LZSet
			long key = edgeKey(current, b);
			int child = findLocal(key);
			if (child != NOT_FOUND) {
				current = child;
				inQuery = false;
			} else {
				addLocal(key, nextLocalId++);
				setSize++;
				current = 0;
				inQuery = true;
			}
		}
	}

	private void allocateLocal(int capacity) {
		localKeys = new long[capacity];
		localValues = new int[capacity];
		localStamps = new int[capacity];
		localMask = capacity - 1;
	}

	private int slot(long key) {
		return (int)((key * 0x9E3779B97F4A7C15L) >>> 32) & localMask;
	}

	private int findLocal(long key) {
		int i = slot(key);
		while (localStamps[i] == epoch) {
			if (localKeys[i] == key) {
				return localValues[i];
			}
			i = (i + 1) & localMask;
		}
		return NOT_FOUND;
	}

	private void addLocal(long key, int value) {
		if ((localCount + 1) * 2 > localKeys.length) {
			growLocal();
		}
		insertLocal(key, value);
		localCount++;
	}

	private void insertLocal(long key, int value) {
		int i = slot(key);
		while (localStamps[i] == epoch) {
			i = (i + 1) & localMask;
		}
		localKeys[i] = key;
		localValues[i] = value;
		localStamps[i] = epoch;
	}

	private void growLocal() {
		long[] keys = localKeys;
		int[] values = localValues;
		int[] stamps = localStamps;
		allocateLocal(keys.length * 2);
		for (int i=0; i<keys.length; i++) {
			if (stamps[i] == epoch) {
				insertLocal(keys[i], values[i]);
			}
		}
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
		int allowedMaxUnmatched = 1 + (int)(threshold * querySetSize / (1 - threshold));

		reset();
		int pos = code.getBytePosition(startPos);
		int windowSize = endPos - startPos;
		// For each token position, update LZSet and LZJD
		for (int t=1; t<=windowSize; t++) {
			int nextPos = code.getBytePosition(startPos + t);
			scan(buf, pos, nextPos);
			pos = nextPos;

			int unmatched = setSize - intersection;
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
		reset();
		scan(buf, 0, buf.length);
		int unionSize = querySetSize + setSize - intersection;
		return (double)(unionSize - intersection) / unionSize;
	}

	@Override
	public void close() {
		// No resource used
	}

}
