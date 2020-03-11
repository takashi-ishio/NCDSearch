package ncdsearch.postfilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ncdsearch.postfilter.debug.DistanceDisFiltering;
import ncdsearch.postfilter.debug.DistanceTopFiltering;
import ncdsearch.postfilter.debug.NoClustering;
import ncdsearch.postfilter.debug.RemoveDistanceDisFiltering;
import ncdsearch.postfilter.debug.RemoveDistanceTopFiltering;
import ncdsearch.postfilter.strategy.Average;
import ncdsearch.postfilter.strategy.Clustering;
import ncdsearch.postfilter.strategy.DistanceClustering;
import ncdsearch.postfilter.strategy.GroupAverage;
import ncdsearch.postfilter.strategy.Longest;
import ncdsearch.postfilter.strategy.NewmanFast;
import ncdsearch.postfilter.strategy.PathClustering;
import ncdsearch.postfilter.strategy.Shortest;



public class Main {
	private static String clusteringStrategy = "EXGA";
	private static String distanceAlgorithm = "ncd";
	private static String checkN = "Dis0.1";
	private static double exDistanceThreshold = 0.35;
	/*optional*/
	private static double clusterDistance = 0;

	//Optional Param
	private static final int CLUSTER_REP_N = 10;
	private static final int CLUSTER_TOP_K = 10000;
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
		
		// Load nodes (code fragments) from file and performs clustering
		ArrayList<JsonNode> nodes = loadFromFile(json);

		// Translate a distance threshold into a Top-N threshold (if given) 
		int allTopN = 10; 
		if (checkN.startsWith("Dis")) {
			double distanceThreshold = Double.parseDouble(checkN.substring("Dis".length()));
			allTopN = countDistanceThreshold(nodes, distanceThreshold);
		} else if (checkN.startsWith("Top")) {
			allTopN = Integer.parseInt(checkN.substring("Top".length()));
		}
		
		// Select nodes from clusters
		Clustering c = getAlgorithm(clusteringStrategy, distanceAlgorithm, CLUSTER_NUM, exDistanceThreshold, clusterDistance, nodes);
		Clusters cs = new Clusters(c, CLUSTER_REP_N, nodes);
 		Set<JsonNode> selected; 
		if (isRemoveClustering) {
			selected = cs.getRemovedFilteredNodes(allTopN, CLUSTER_TOP_K);
		} else {
			selected = cs.getFilteredNodes(allTopN, CLUSTER_TOP_K);
		}
		
		// Add attributes to nodes
		annotateElementsWithFilteringResult(cs, selected);
		
		// Write a result into STDOUT
		ResultJson rj = new ResultJson(nodes);
		try {
			ObjectMapper mapper = new ObjectMapper();
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString((Object) rj));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private static int countDistanceThreshold(List<JsonNode> nodes, double distanceThreshold) {
		int count = 0;
		for (JsonNode node: nodes) {
			if (JsonNodeInfo.getNodeDistance(node) <= distanceThreshold) {
				count++;
			}
		}
		return count;
	}
	

	private static final String KEY_RESULT = "Result";

	
	/**
	 * Load json nodes from file
	 * @param file
	 * @return
	 */
	private static ArrayList<JsonNode> loadFromFile(File file) {
		ArrayList<JsonNode> nodes = new ArrayList<>(); 
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(file);
			for (JsonNode node: root.get(KEY_RESULT)) {
				nodes.add(node);
			}
			Clusters.sortByDistance(nodes);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return nodes;
	}
	

	private static void annotateElementsWithFilteringResult(Clusters clusters, Set<JsonNode> selected) {
		int rankTotal = 0;
		for (int i=0; i<clusters.getClusterCount(); i++) {
			List<JsonNode> cluster = clusters.getClusterContents().get(i);
			for (int j=0; j<cluster.size(); j++) {
				rankTotal++;
				ObjectNode node = (ObjectNode)cluster.get(j);
				node.put("ClusterID", i+1);
				node.put("ShouldCheck", selected.contains(node));
				node.put("RankInCluster", j+1);
				node.put("RankTotal", rankTotal);
			}
			System.err.println("RankInCluster:" + cluster.size());
		}
		System.err.println("ClusterID:" + clusters.getClusterCount() + ", RankTotal:" + rankTotal);
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

	public static Clustering getAlgorithm(String clustringStrategy, String distanceAlgorithm, int clusterNum, double exDistanceThreshold, double clusterDistance, ArrayList<JsonNode> allNode) {
		// EX-Clustering: Terminated when distance between clusters reached a threshold
		if (clustringStrategy.startsWith("EX")) {
			DistanceClustering c;
			if (clustringStrategy.equals("EXSH")) {
				c = new Shortest(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold);
			} else if (clustringStrategy.equals("EXLO")) {
				c = new Longest(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold);
			} else if (clustringStrategy.equals("EXGA")) {
				c = new GroupAverage(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold);
			} else if (clustringStrategy.equals("EXAV")) {
				c = new Average(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold);
			} else if (clustringStrategy.startsWith("EXDF")) {
				if (clustringStrategy.startsWith("EXDFT")) {
					c = new DistanceTopFiltering(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold,
							clusterDistance);
				} else {
					/* EXDFD */
					c = new DistanceDisFiltering(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold,
							clusterDistance);
				}
			} else {
				// System.err.println("Not Supported Strategy: " + clustringStrategy);
				System.err.println("ExNo Clustering: ");
				c = new NoClustering(allNode, distanceAlgorithm, clusterNum);
			}
			c.enableExClustering();
			return c;
		} else if (clustringStrategy.startsWith("RM")) {
			Clustering c;
			if (clustringStrategy.startsWith("RMEXDFT")) {
				c = new RemoveDistanceTopFiltering(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold,
						clusterDistance);
			} else {
				/* RMEXDFD */
				c = new RemoveDistanceDisFiltering(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold,
						clusterDistance);
			}
			return c;
		} else {
			Clustering c;
			// Regular clustering: clusters are merged to obtain k clusters
			if (clustringStrategy.equals("DIR") || clustringStrategy.equals("FILE")) {
				c = new PathClustering(allNode, clustringStrategy);
			} else if (clustringStrategy.equals("SH")) {
				c = new Shortest(allNode, distanceAlgorithm, clusterNum);
			} else if (clustringStrategy.equals("LO")) {
				c = new Longest(allNode, distanceAlgorithm, clusterNum);
			} else if (clustringStrategy.equals("GA")) {
				c = new GroupAverage(allNode, distanceAlgorithm, clusterNum);
			} else if (clustringStrategy.equals("AV")) {
				c = new Average(allNode, distanceAlgorithm, clusterNum);
			} else if (clustringStrategy.equals("NF")) {
				c = new NewmanFast(allNode, distanceAlgorithm);
			} else {
				// System.err.println("Not Supported Strategy: " + clustringStrategy);
				// System.err.println("No Clustering: ");
				c = new NoClustering(allNode, distanceAlgorithm, clusterNum);
			}
			return c;
		}
	}
	
}
