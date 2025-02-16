package ncdsearch.postfilter.strategy;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is a super class representing a clustering algorithm.
 */
public abstract class Clustering {

	protected List<JsonNode> allNode;
	protected String strategy;

	public Clustering(List<JsonNode> allNode, String strategy) {
		this.allNode = allNode;
		this.strategy = strategy;
	}

	public abstract List<List<JsonNode>> clustering();

}
