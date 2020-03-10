package ncdsearch.postfilter.evaluate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.Answers;
import ncdsearch.postfilter.Clusters;
import ncdsearch.postfilter.JsonNodesInfo;

public class IdealEvaluate extends Evaluate {
	public IdealEvaluate(String checkN, int clusterTopN, boolean isRemoveClustering) {
		super(checkN, clusterTopN, isRemoveClustering);
	}

	@Override
	public void evaluate(Clusters cs, Answers a, String path) {
		Clusters ics = getIdealClusters(cs, a);
		System.out.println("Filtered Node: " + ics.getNodeSize());
		System.out.println("Filtered Dir: " + ics.getClusterRepsSize());
		pushToTotal(cs, a, ics);
		printResult(cs, a, ics);
	}

	public Clusters getIdealClusters(Clusters cs, Answers a) {
		Clusters ics = new Clusters();
		Set<List<JsonNode>> set = new HashSet<>();
		for (List<JsonNode> nodes : cs.getRepJsonMap().values()) {
			if (set.contains(nodes))
				continue;
			List<JsonNode> sortedNodes = JsonNodesInfo.getSortedListbyDistance(nodes);
			if (CompareNodes.isContainInAnswer(nodes, a.getAllNode())) {
				ics.addClusterReps(sortedNodes);
				ics.addAllNode(nodes);
				for (JsonNode node : sortedNodes) {
					ics.putRepJsonMap(node, cs.getRepJsonMap().get(node));
				}
			} else {
				nonAnswerRepSize += Math.min(allTopN, nodes.size());
				//				nonAnswerRepSize += Math.min(10, nodes.size());
			}
			set.add(nodes);
		}
		return ics;
	}
}
