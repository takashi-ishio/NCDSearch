package ncdsearch.postfilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;



public class Main {
	private static String clusteringStrategy = "EXGA";
	private static String distanceAlgorithm = "ncd";
	private static String checkN = "Dis0.1";
	private static double exDistanceThreshold = 0.35;
	/*optional*/
	private static double clusterDistance = 0;

	//Optional Param
	private static final int CLUSTER_REP_N = 10;
	private static final int CLUSTER_TOP_N_2 = 10000;
	private static final int CLUSTER_NUM = 5;

	public static void main(String[] args) {
		
		File json = new File(args[0]);
		
		if (args.length > 1) {
			checkN = args[1];
			exDistanceThreshold = Double.parseDouble(args[2]);
			clusteringStrategy = args[3];
		}
		if (args.length > 4) {
			clusterDistance = Double.parseDouble(args[4]);

		}
		boolean isRemoveClustering;
		if (clusteringStrategy.startsWith("RM")) {
			isRemoveClustering = true;
			clusteringStrategy = clusteringStrategy.substring(2);
		} else {
			isRemoveClustering = false;
		}

		if (clusteringStrategy.startsWith("EXDF")) {
			if (isRemoveClustering)
				clusteringStrategy = "RM" + clusteringStrategy;
		}
		
		Clusters cs = new Clusters(clusteringStrategy, distanceAlgorithm, CLUSTER_REP_N, CLUSTER_NUM, exDistanceThreshold, clusterDistance, loadFromFile(json));
		
		int allTopN = 10; 
		if (checkN.startsWith("Dis")) {
			allTopN = 0;
			double distanceThreshold = Double.parseDouble(checkN.substring("Dis".length()));
			if (isRemoveClustering) {
				for (JsonNode node : cs.getAllNode()) {
					if (JsonNodeInfo.getNodeDistance(node) > distanceThreshold) {
						allTopN++;
					}
				}
			} else {
				for (JsonNode node : cs.getAllNode()) {
					if (JsonNodeInfo.getNodeDistance(node) <= distanceThreshold) {
						allTopN++;
					}
				}
			}
		} else if (checkN.startsWith("Top")) {
			allTopN = Integer.parseInt(checkN.substring("Top".length()));
		}
		
 		ArrayList<JsonNode> selected; 
		if (isRemoveClustering) {
			selected = cs.getRemovedFilteredNodes(allTopN, CLUSTER_TOP_N_2);
		} else {
			selected = cs.getFilteredNodes(allTopN, CLUSTER_TOP_N_2);
		}
		
		annotateElementsWithFilteringResult(cs, selected);
		
		ResultJson rj = new ResultJson(cs.getAllNode());
		try {
			ObjectMapper mapper = new ObjectMapper();
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString((Object) rj));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	private static final String KEY_RESULT = "Result";

	private static ArrayList<JsonNode> loadFromFile(File file) {
		ArrayList<JsonNode> nodes = new ArrayList<>(); 
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(file);
			for (JsonNode node : root.get(KEY_RESULT)) {
				nodes.add(node);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return nodes;
	}
	

	private static void annotateElementsWithFilteringResult(Clusters clusters, ArrayList<JsonNode> selected) {
		int clusterID = 1;
		int rankTotal = 1;
		for (List<JsonNode> list : clusters.getClusterReps()) {
			int rankInCluster = 1;
			for (JsonNode node : clusters.getRepJsonMap().get(list.get(0))) {
				((ObjectNode) node).put("ClusterID", clusterID);
				if (selected.contains(node)) {
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

	/**
	 * A wrapper class for generating a json file with appropriate attribute name
	 */
	public static class ResultJson {
		List<JsonNode> Result;

		public ResultJson(List<JsonNode> Result) {
			this.Result = Result;
		}

		public List<JsonNode> getResult() {
			return Result;
		}
	}

}
