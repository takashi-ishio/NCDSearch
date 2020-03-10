package ncdsearch.postfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class Filter {
	protected int allTopN;
	protected double distanceThreshold;
	protected int clusterTopN;

	public Filter(int allTopN, int clusterTopN) {
		this.allTopN = allTopN;
		this.clusterTopN = clusterTopN;
	}

	public ArrayList<JsonNode> getFilteredNodes(Clusters cs) {
		ArrayList<JsonNode> selected = new ArrayList<>();
		for (List<JsonNode> nodes : cs.getClusterReps()) {
			List<JsonNode> sortedNodes = getSortedListbyDistance(nodes);
			if (containTopN(nodes, cs.getAllNode())) {
				List<JsonNode> list = new ArrayList<>(cs.getRepJsonMap().get(sortedNodes.get(0)));
				selected.addAll(list.subList(0, Math.min(list.size(), clusterTopN)));
			}
		}
		return selected;
	}
	
	/*ascending order*/
	public static List<JsonNode> getSortedListbyDistance(List<JsonNode> nodes) {
		Collections.sort(nodes,
				(p1, p2) -> Double.compare(p1.get("Distance").asDouble(), p2.get("Distance").asDouble()));
		return nodes;
	}

	private boolean containTopN(List<JsonNode> nodes, List<JsonNode> allNode) {
		for (int i = 0; i < this.allTopN; i++) {
			JsonNode minNode = getSortedListbyDistance(allNode).get(i);
			if (nodes.contains(minNode)) {
				return true;
			}
		}
		return false;
	}


	public ArrayList<JsonNode> getRemovedFilteredNodes(Clusters cs) {
		ArrayList<JsonNode> selected = new ArrayList<>();
		for (List<JsonNode> nodes : cs.getClusterReps()) {
			List<JsonNode> sortedNodes = getSortedListbyDistance(nodes);
			if (!containNontopN(nodes, cs.getAllNode())) {

				List<JsonNode> list = new ArrayList<>(cs.getRepJsonMap().get(sortedNodes.get(0)));
				selected.addAll(list.subList(0, Math.min(list.size(), clusterTopN)));
			}
		}
		return selected;
	}



	private boolean containNontopN(List<JsonNode> nodes, List<JsonNode> allNode) {
		for (int i = allNode.size() - 1; i >= allNode.size() - this.allTopN; i--) {
			JsonNode maxNode = getSortedListbyDistance(allNode).get(i);
			if (nodes.contains(maxNode)) {
				return true;
			}
		}
		return false;
	}
}
