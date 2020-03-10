package ncdsearch.postfilter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class Answers {
	protected List<JsonNode> allNode = new ArrayList<>();

	public void addNode(JsonNode node) {
		allNode.add(node);
	}

	public List<JsonNode> getAllNode() {
		return allNode;
	}

	public int getAllNodeSize() {
		return allNode.size();
	}
}
