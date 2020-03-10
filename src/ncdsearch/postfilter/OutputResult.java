package ncdsearch.postfilter;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OutputResult {
	Clusters clusters;
	Answers answers;
	Clusters filteredClusters;
	String path;

	public OutputResult(Clusters cs, Clusters fcs, String path) {
		this.clusters = cs;
		this.filteredClusters = fcs;
		this.path = path;
	}

	public OutputResult(Clusters cs, Answers a, Clusters fcs, String path) {
		this.clusters = cs;
		this.answers = a;
		this.filteredClusters = fcs;
		this.path = path;
	}

	public void print() {
		addElementToJson();
		ResultJson rj = new ResultJson(clusters.getAllNode());
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<String> lines = new ArrayList<>();
			lines.add(mapper.writeValueAsString((Object) rj));
			Path p;
			if (path.endsWith(".json")) {
				p = Paths.get(path.substring(0, path.lastIndexOf("/")), "filtering-"+
						path.substring(path.lastIndexOf("/")+1));
			} else {
				p = Paths.get(path, "result-rank.json");
			}
			if (Files.exists(p)) {
				Files.delete(p);
			}
			Files.createFile(p);
			Files.write(p, lines, Charset.forName("UTF-8"), StandardOpenOption.WRITE);
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
			System.out.println("RankInCluster:" + rankInCluster);
			clusterID++;
		}
		System.out.println("ClusterID:" + (clusterID - 1) + ", RankTotal:" + (rankTotal - 1));
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
