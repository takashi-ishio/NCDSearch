package ncdsearch.postfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodesInfo {

	public static List<String> getNodesDirs(List<JsonNode> nodes) {
		List<String> dirList = new ArrayList<>();
		for (JsonNode node : nodes) {
			String fullDir = node.get("DirName").asText();
			dirList.add(fullDir.substring(fullDir.indexOf("src") + 4).replace("\\", "/"));
		}
		return dirList;
	}

	public static List<String> getNodesListDirs(List<List<JsonNode>> nodes) {
		List<String> dirList = new ArrayList<>();
		for (List<JsonNode> nodelist : nodes) {
			for (JsonNode node : nodelist) {
				String fullDir = node.get("DirName").asText();
				dirList.add(fullDir.substring(fullDir.indexOf("src") + 4).replace("\\", "/"));
			}
		}
		return dirList;
	}

	public static List<String> getNodeFiles(List<JsonNode> nodes) {
		List<String> fileList = new ArrayList<>();
		for (JsonNode node : nodes) {
			String fullFile = node.get("FileName").asText();
			fileList.add(fullFile.substring(fullFile.indexOf("src") + 4).replace("\\", "/"));
		}
		return fileList;
	}

	public static List<String> getNodesListFiles(List<List<JsonNode>> nodes) {
		List<String> fileList = new ArrayList<>();
		for (List<JsonNode> nodelist : nodes) {
			for (JsonNode node : nodelist) {
				String fullFile = node.get("FileName").asText();
				fileList.add(fullFile.substring(fullFile.indexOf("src") + 4).replace("\\", "/"));
			}
		}
		return fileList;
	}

	public static List<Double> getNodeDistances(List<JsonNode> nodes) {
		List<Double> distanceList = new ArrayList<>();
		for (JsonNode node : nodes) {
			distanceList.add(node.get("Distance").asDouble());
		}
		return distanceList;
	}

	public static List<JsonNode> getRepNodeListbyDir(JsonNode qn, List<List<JsonNode>> nodes) {
		List<JsonNode> repNodelist = new ArrayList<>();
		for (List<JsonNode> nodelist : nodes) {
			for (JsonNode node : nodelist) {
				if (JsonNodeInfo.getNodeDir(node).equals(JsonNodeInfo.getNodeDir(qn))) {
					repNodelist.add(node);
				}
			}
		}
		return getSortedListbyDistance(repNodelist);
	}

	public static List<JsonNode> getRepNodeListbyFile(JsonNode qn, List<List<JsonNode>> nodes) {
		List<JsonNode> repNodelist = new ArrayList<>();
		for (List<JsonNode> nodelist : nodes) {
			for (JsonNode node : nodelist) {
				if (JsonNodeInfo.getNodeFile(node).equals(JsonNodeInfo.getNodeFile(qn))) {
					repNodelist.add(node);
				}
			}
		}
		return getSortedListbyDistance(repNodelist);
	}

	/*ascending order*/
	public static List<JsonNode> getSortedListbyDistance(List<JsonNode> nodes) {
		Collections.sort(nodes,
				(p1, p2) -> Double.compare(p1.get("Distance").asDouble(), p2.get("Distance").asDouble()));
		return nodes;
	}

	//TODO  Comparison method violates its general contract
	//	public static int isBetterThan(JsonNode one, JsonNode another) {
	//		// Distance: Lower is better
	//		if (JsonNodeInfo.getNodeDistance(one) > JsonNodeInfo.getNodeDistance(another))
	//			return 1;
	//		if (JsonNodeInfo.getNodeDistance(one) < JsonNodeInfo.getNodeDistance(another))
	//			return -1;
	//		else {
	//			// Shorter is better
	//			int onelen = JsonNodeInfo.getNodeEndChar(one) - JsonNodeInfo.getNodeStartChar(one);
	//			int anotherlen = JsonNodeInfo.getNodeEndChar(another) - JsonNodeInfo.getNodeStartChar(another);
	//			if (onelen < anotherlen)
	//				return 1;
	//			else if (onelen > anotherlen)
	//				return -1;
	//			else {
	//				return JsonNodeInfo.getNodeStartChar(one) < JsonNodeInfo.getNodeStartChar(another) ? 1 : -1;
	//			}
	//		}
	//	}

	public static JsonNode getRepNodebyFile(JsonNode qn, List<List<JsonNode>> nodes) {
		for (List<JsonNode> nodelist : nodes) {
			for (JsonNode node : nodelist) {
				if (JsonNodeInfo.getNodeFile(node).equals(JsonNodeInfo.getNodeFile(qn))) {
					return node;
				}
			}
		}
		return null;
	}
}
