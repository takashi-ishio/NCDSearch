package ncdsearch.postfilter.evaluate;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.Clusters;
import ncdsearch.postfilter.JsonNodesInfo;

public class Filter {
	protected int allTopN;
	protected double distanceThreshold;
	protected boolean isRemoveClustering;
	protected int clusterTopN;

	public Filter(int allTopN, int clusterTopN, boolean isRemoveClustering) {
		this.allTopN = allTopN;
		this.clusterTopN = clusterTopN;
		this.isRemoveClustering = isRemoveClustering;
	}

	public Clusters getFilteredClusters(Clusters cs) {
		Clusters fcs = new Clusters();
		for (List<JsonNode> nodes : cs.getClusterReps()) {
			List<JsonNode> sortedNodes = JsonNodesInfo.getSortedListbyDistance(nodes);
			//if (!isContainLongNode(nodes, cs.getAllNode())) {
			if (isContainMinNode(nodes, cs.getAllNode())) {
				//if (isContainInAnswer(nodes, a.getAllNode())) {
				fcs.addClusterReps(sortedNodes);

				addNode(cs, fcs, sortedNodes);

				for (JsonNode node : sortedNodes) {
					fcs.putRepJsonMap(node, cs.getRepJsonMap().get(node));
				}
				//} else {
				//	nonAnswerRepSize += sortedNodes.size();
				//}
			}
			//printRank(cs, nodes);
		}
		return fcs;
	}

	public Clusters getRemovedFilteredClusters(Clusters cs) {
		Clusters fcs = new Clusters();
		for (List<JsonNode> nodes : cs.getClusterReps()) {
			List<JsonNode> sortedNodes = JsonNodesInfo.getSortedListbyDistance(nodes);
			if (!isContainMaxNode(nodes, cs.getAllNode())) {
				//if (isContainInAnswer(nodes, a.getAllNode())) {
				fcs.addClusterReps(sortedNodes);

				addNode(cs, fcs, sortedNodes);

				for (JsonNode node : sortedNodes) {
					fcs.putRepJsonMap(node, cs.getRepJsonMap().get(node));
				}
			}
		}
		return fcs;
	}

	protected void addNode(Clusters cs, Clusters fcs, List<JsonNode> sortedNodes) {
		//fcs.addAllNode(cs.getRepJsonMap().get(sortedNodes.get(0)));
		List<JsonNode> list = new ArrayList<>(cs.getRepJsonMap().get(sortedNodes.get(0)));
		fcs.addAllNode(list.subList(0, Math.min(list.size(), clusterTopN)));
	}

//	private boolean isContainLongNode(List<JsonNode> nodes, List<JsonNode> allNode) {
//		for (JsonNode node : nodes) {
//			if (JsonNodeInfo.getNodeDistance(node) > distanceThreshold) {
//				return true;
//			}
//		}
//		return false;
//	}

	private boolean isContainMinNode(List<JsonNode> nodes, List<JsonNode> allNode) {
		for (int i = 0; i < this.allTopN; i++) {
			JsonNode minNode = JsonNodesInfo.getSortedListbyDistance(allNode).get(i);
			if (nodes.contains(minNode)) {
				return true;
			}
		}
		return false;
	}

	private boolean isContainMaxNode(List<JsonNode> nodes, List<JsonNode> allNode) {
		for (int i = allNode.size() - 1; i >= allNode.size() - this.allTopN; i--) {
			JsonNode maxNode = JsonNodesInfo.getSortedListbyDistance(allNode).get(i);
			if (nodes.contains(maxNode)) {
				return true;
			}
		}
		return false;
	}
}
