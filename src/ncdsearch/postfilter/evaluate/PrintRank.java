package ncdsearch.postfilter.evaluate;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.Answers;
import ncdsearch.postfilter.Clusters;
import ncdsearch.postfilter.JsonNodesInfo;

public class PrintRank {
	void printAnswerRank(Clusters cs, Answers a) {
		List<JsonNode> sortedList = JsonNodesInfo.getSortedListbyDistance(cs.getAllNode());
		for (int i = 0; i < sortedList.size(); i++) {
			if (CompareNodes.isContainInAnswer(sortedList.get(i), a.getAllNode())) {
				System.out.println("Answer Rank: " + (i + 1));
			}
		}
	}

	void printNodeRank(Clusters cs, Clusters fcs) {
		System.out.println("----");
		List<JsonNode> sortedList = JsonNodesInfo.getSortedListbyDistance(cs.getAllNode());
		for (JsonNode node : fcs.getAllNode()) {
			for (int i = 0; i < sortedList.size(); i++) {
				if (node.equals(sortedList.get(i))) {
					System.out.println("Rank: " + (i + 1));
				}
			}
		}
	}

	void printNodeRank(Clusters cs, List<JsonNode> nodeList) {
		System.out.println("----");
		List<JsonNode> sortedList = JsonNodesInfo.getSortedListbyDistance(cs.getAllNode());
		for (JsonNode node : nodeList) {
			for (int i = 0; i < sortedList.size(); i++) {
				if (node.equals(sortedList.get(i))) {
					System.out.println("Rank: " + (i + 1));
					break;
				}
			}
		}
	}

}
