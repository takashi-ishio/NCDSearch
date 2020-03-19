package ncdsearch.postfilter.debug;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.strategy.Cluster;
import ncdsearch.postfilter.strategy.DistanceClustering;

public class NoClustering extends DistanceClustering {

	public NoClustering(List<JsonNode> allNode, String strategy) {
		super(allNode, strategy, 0);
	}

	@Override
	public List<List<JsonNode>> clustering() {
		List<List<JsonNode>> nodeList = new ArrayList<>();
		for (JsonNode node : allNode) {
			List<JsonNode> list = new ArrayList<>();
			list.add(node);
			nodeList.add(list);
		}
		return nodeList;
	}

	@Override
	protected void update() {

	}

	@Override
	protected double calcDistance(Cluster c1, Cluster c2) {
		return 0;
	}
}
