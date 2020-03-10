package ncdsearch.postfilter;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OutputResult {
	Clusters clusters;
	Clusters filteredClusters;

	public OutputResult(Clusters cs, Clusters fcs) {
		this.clusters = cs;
		this.filteredClusters = fcs;
	}

	public void print() {
		addElementToJson();
		ResultJson rj = new ResultJson(clusters.getAllNode());
		try {
			ObjectMapper mapper = new ObjectMapper();
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString((Object) rj));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*@TODO jsonのTotalでのRankのアルゴリズムを考える
	 *
	 * */
	private void addElementToJson() {
		int clusterID = 1;
		int rankTotal = 1;
		for (List<JsonNode> list : clusters.getClusterReps()) {
			int rankInCluster = 1;
			for (JsonNode node : clusters.getRepJsonMap().get(list.get(0))) {
				((ObjectNode) node).put("ClusterID", clusterID);
				if (filteredClusters.getAllNode().contains(node)) {
					((ObjectNode) node).put("ShouldCheck", "true");
				} else {
					((ObjectNode) node).put("ShouldCheck", "false");
				}
				((ObjectNode) node).put("RankInCluster", rankInCluster);
				((ObjectNode) node).put("RankTotal", rankTotal);
				rankInCluster++;
				rankTotal++;
			}
			System.err.println("RankInCluster:" + rankInCluster);
			clusterID++;
		}
		System.err.println("ClusterID:" + (clusterID - 1) + ", RankTotal:" + (rankTotal - 1));
	}

	public class ResultJson {
		List<JsonNode> Result;

		public ResultJson(List<JsonNode> Result) {
			this.Result = Result;
		}

		public List<JsonNode> getResult() {
			return Result;
		}
	}
}
