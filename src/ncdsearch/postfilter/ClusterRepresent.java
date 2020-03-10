package ncdsearch.postfilter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class ClusterRepresent {
	private int topN;

	public ClusterRepresent(int topN) {
		this.topN = topN;
	}

	public List<JsonNode> getClusterReps(List<JsonNode> nodes) {
		List<JsonNode> sortedList = JsonNodesInfo.getSortedListbyDistance(nodes);
		if (sortedList.size() <= topN) {
			return sortedList;
		} else {
			List<JsonNode> list = new ArrayList<>(sortedList);
			return list.subList(0, topN);
		}
	}
}
