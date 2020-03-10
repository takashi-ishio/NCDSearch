package ncdsearch.postfilter.evaluate;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.JsonNodeInfo;

public class CompareNodes {
	static boolean isContainInAnswer(JsonNode node, List<JsonNode> answerNodes) {
		for (JsonNode aNode : answerNodes) {
			if (JsonNodeInfo.getNodeFile(node).equals(JsonNodeInfo.getNodeAnswerFile(aNode))) {
				if (JsonNodeInfo.getNodeEndLine(node) >= JsonNodeInfo.getNodeSLine(aNode) &&
						JsonNodeInfo.getNodeStartLine(node) <= JsonNodeInfo.getNodeELine(aNode)) {
					return true;
				}
			}
		}
		return false;
	}

	static boolean isContainInAnswer(List<JsonNode> nodes, List<JsonNode> answerNodes) {
		for (JsonNode aNode : answerNodes) {
			for (JsonNode node : nodes) {
				if (JsonNodeInfo.getNodeFile(node).equals(JsonNodeInfo.getNodeAnswerFile(aNode))) {
					if (JsonNodeInfo.getNodeEndLine(node) >= JsonNodeInfo.getNodeSLine(aNode) &&
							JsonNodeInfo.getNodeStartLine(node) <= JsonNodeInfo.getNodeELine(aNode)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	static boolean isContainInResult(JsonNode node, List<JsonNode> resultNodes) {
		for (JsonNode rNode : resultNodes) {
			if (JsonNodeInfo.getNodeFile(rNode).equals(JsonNodeInfo.getNodeAnswerFile(node))) {
				if (JsonNodeInfo.getNodeEndLine(rNode) >= JsonNodeInfo.getNodeSLine(node) &&
						JsonNodeInfo.getNodeStartLine(rNode) <= JsonNodeInfo.getNodeELine(node)) {
					return true;
				}
			}
		}
		return false;
	}
}
