package ncdsearch.postfilter.strategy;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.SearchConfiguration;
import ncdsearch.comparison.ICodeDistanceStrategy;
import ncdsearch.comparison.TokenSequence;
import ncdsearch.postfilter.JsonNodeInfo;

/**
 * Wrap Fragment class to store the distance strategy and distance
 * This class is a vertex of weighted fully connected graph
 * @author ito-k
 *
 */
public class Component {
	/**
	 * Map to reduce calc distance between code fragment
	 * Key: Pair of code fragment
	 * Value: Distance of key pair
	 */
	private static Map<JsonNodePair, Double> edgeMap;

	private JsonNode node;
	private TokenSequence token;
	private ICodeDistanceStrategy strategy;

	/**
	 * Fragment and distance strategy are taken as given
	 * @param fragment <p>another Fragment object</p>
	 * @param strategy <p>distance strategy between code fragments</p>
	 */
	public Component(JsonNode node, String strategy) {
		this.node = node;
		this.token = new TokenSequence(JsonNodeInfo.getNodeTokens(this.node));
		this.strategy = SearchConfiguration.createStrategy(strategy, token);
		if (edgeMap == null)
			edgeMap = new HashMap<>();
	}

	public Component(JsonNode node, ICodeDistanceStrategy strategy) {
		this.node = node;
		this.strategy = strategy;
		if (edgeMap == null)
			edgeMap = new HashMap<>();
	}

	/**
	 * <p>Compute distance between another component</p>
	 * If the distance of pair has already been computed, it will bet got from edgeMap.
	 * @param another another Component object
	 * @return distance between another component
	 */
	public double computeDistance(Component another) {
		if (strategy == null) {
			System.err.println("No distance strategy");
			System.exit(1);
		}
		JsonNodePair pair = new JsonNodePair(this.node, another.node);
		Double distance = edgeMap.get(pair);
		if (distance == null) {
			distance = strategy.computeDistance(another.token);
			//			System.err.println(this.fragment.getDistance() + ", " + another.fragment.getDistance() + ", " + distance);
			//			if(distance > 0.5) distance = 1.0;
			edgeMap.put(pair, distance);
		}
		return distance;
	}

	/**
	 * Getter of fragment
	 * @return fragment <p>code fragment in component</p>
	 */
	public JsonNode getJsonNode() {
		return node;
	}

	public int compare(Component another) {
		// Similar (Lower distance) is better
		int comparison = Double.compare(JsonNodeInfo.getNodeDistance(this.node), JsonNodeInfo.getNodeDistance(another.node));
		if (comparison != 0) return comparison;

		// Shorter is better
		int thislen = JsonNodeInfo.getNodeEndLine(node) - JsonNodeInfo.getNodeStartLine(node);
		int anotherlen = JsonNodeInfo.getNodeEndLine(another.node) - JsonNodeInfo.getNodeStartLine(another.node);
		if (thislen != anotherlen) return Integer.compare(thislen, anotherlen);

		// Younger file path is better 
		int filePath = JsonNodeInfo.getNodeFile(this.node).compareTo(JsonNodeInfo.getNodeFile(another.node));
		if (filePath != 0) return filePath;

		// Earlier line is better
		int pos = Integer.compare(JsonNodeInfo.getNodeStartLine(this.node), JsonNodeInfo.getNodeStartLine(another.node));
		if (pos != 0) return pos;
		pos = Integer.compare(JsonNodeInfo.getNodeStartChar(this.node), JsonNodeInfo.getNodeStartChar(another.node));
		return pos;
	}

	/**
	 * Wrapper of key pair
	 * This class contains two Fragment, and ignore which Fragment is first one
	 * @author ito-k
	 *
	 */
	private static class JsonNodePair {
		private JsonNode c1;
		private JsonNode c2;

		public JsonNodePair(JsonNode node, JsonNode node2) {
			this.c1 = node;
			this.c2 = node2;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof JsonNodePair))
				return false;
			JsonNodePair key = (JsonNodePair) o;
			if ((c1 == key.c1 && c2 == key.c2) || (c1 == key.c2 && c2 == key.c1))
				return true;
			return false;
		}

		@Override
		public int hashCode() {
			return c1.hashCode() + c2.hashCode();
		}

	}

}
