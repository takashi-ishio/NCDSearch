package ncdsearch.postfilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
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


/**
 * This is an experimental implementation of post-filtering 
 * developed for a research paper (written in Japanese). 
 * DOI: 10.14923/transinfj.2020JDL8009
 *
 * This program takes as input a JSON file created by SearchMain and 
 * apply clustering-based filtering to the result.
 * Options "-json" and "-pos" are required to generate an analyzable JSON file.
 * 
 * This program produces a JSON file including a sorted list of 
 * code fragments.  Using a clustering algorithm, the program adds
 * the "ShouldCheck" field to code fragments. 
 * 
 * Note that this program is experimental.  Use it at your own risk.
 */
public class ClusteringMain {
	
	private static String clusteringStrategy = "EXGA";
	private static String distanceAlgorithm = "lzjd";
	private static String checkN = "Dis0.1";
	private static double exDistanceThreshold = 0.35;
	/*optional*/
	private static double clusterDistance = 0.25;

	//Optional Param
	private static final int CLUSTER_REP_N = 10;
	private static final int CLUSTER_TOP_K = 10000;
	private static final int CLUSTER_NUM = 5;

	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.err.println("Arguments: ncdsearch-result.json [Base-Threshold] [Neighborhood-Threshold]");
			System.err.println("Arguments: ncdsearch-result.json -cluster=[Clustering-algorithm]:[Inter-cluster-distance-threshold]");
		}
		
		File json = new File(args[0]);
		// Load nodes (code fragments) from file and performs clustering
		ArrayList<JsonNode> nodes = loadFromFile(json);

		
		if (args.length == 3) {
			double distanceThreshold = Double.parseDouble(args[1]);
			double neighborhoodThreshold = Double.parseDouble(args[2]);
			
			// Select elements 
			Clustering c = new RemoveDistanceDisFiltering(nodes, distanceAlgorithm, 0, distanceThreshold, neighborhoodThreshold);
			List<List<JsonNode>> clusters = c.clustering();
	 		Set<JsonNode> selected = Collections.newSetFromMap(new IdentityHashMap<JsonNode, Boolean>()); 
	 		for (List<JsonNode> cluster: clusters) {
	 			selected.addAll(cluster);
	 		}
			
			// Add attributes to nodes
	 		annotateElementsWithFilteringResult(nodes, selected);
			
			// Write a result into STDOUT
			ResultJson rj = new ResultJson(nodes);
			try {
				ObjectMapper mapper = new ObjectMapper();
				System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString((Object) rj));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else if (args.length == 2) {
			String config = args[1];
			if (config.startsWith("-cluster=")) {
				String[] tokens = config.substring("-cluster=".length()).split(":");
				String strategyName = tokens[0];
				double threshold = 0.5;
				if (tokens.length > 1) threshold = Double.parseDouble(tokens[1]);
				Clustering c = getAlgorithm(nodes, strategyName, threshold);
				Clusters cs = new Clusters(c, 1, nodes);
				annotateClusters(cs);
				// Write a result into STDOUT
				ResultJson rj = new ResultJson(nodes);
				try {
					ObjectMapper mapper = new ObjectMapper();
					System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString((Object) rj));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("Arguments: ncdsearch-result.json -cluster=[Clustering-algorithm]:[Inter-cluster-distance-threshold]");
			}

		} else {
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
	
	private static void annotateElementsWithFilteringResult(List<JsonNode> elements, Set<JsonNode> selected) {
		for (JsonNode element: elements) {
			ObjectNode node = (ObjectNode)element;
			node.put("ShouldCheck", selected.contains(node));
		}
	}

	private static void annotateClusters(Clusters clusters) {
		for (int i=0; i<clusters.getClusterCount(); i++) {
			List<JsonNode> cluster = clusters.getClusterContents().get(i);
			for (int j=0; j<cluster.size(); j++) {
				ObjectNode node = (ObjectNode)cluster.get(j);
				node.put("ClusterID", i+1);
				node.put("RankInCluster", j+1);
			}
		}
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
	
	private static Clustering getAlgorithm(List<JsonNode> nodes, String clusteringStrategy, double distanceThreshold) {
		int dummyClusterNum = 0;
		Clustering c;
		if (clusteringStrategy.equals("SH")) {
			c = new Shortest(nodes, distanceAlgorithm, dummyClusterNum, distanceThreshold);
		} else if (clusteringStrategy.equals("LO")) {
			c = new Longest(nodes, distanceAlgorithm, dummyClusterNum, distanceThreshold);
		} else if (clusteringStrategy.equals("GA")) {
			c = new GroupAverage(nodes, distanceAlgorithm, dummyClusterNum, distanceThreshold);
		} else if (clusteringStrategy.equals("AV")) {
			c = new Average(nodes, distanceAlgorithm, dummyClusterNum, distanceThreshold);
		} else if (clusteringStrategy.equals("NF")) {
			c = new NewmanFast(nodes, distanceAlgorithm);
		} else {
			c = new NoClustering(nodes, distanceAlgorithm);
		}
		return c;
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
				c = new NoClustering(allNode, distanceAlgorithm);
			}
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
				c = new NoClustering(allNode, distanceAlgorithm);
			}
			return c;
		}
	}
	
}
