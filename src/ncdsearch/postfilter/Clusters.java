package ncdsearch.postfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.debug.DistanceDisFiltering;
import ncdsearch.postfilter.debug.DistanceTopFiltering;
import ncdsearch.postfilter.debug.NoClustering;
import ncdsearch.postfilter.debug.RemoveDistanceDisFiltering;
import ncdsearch.postfilter.debug.RemoveDistanceTopFiltering;
import ncdsearch.postfilter.strategy.Average;
import ncdsearch.postfilter.strategy.Clustering;
import ncdsearch.postfilter.strategy.GroupAverage;
import ncdsearch.postfilter.strategy.Longest;
import ncdsearch.postfilter.strategy.NewmanFast;
import ncdsearch.postfilter.strategy.PathClustering;
import ncdsearch.postfilter.strategy.Shortest;

public class Clusters {

	protected List<JsonNode> allNode;
	protected List<List<JsonNode>> clusterReps = new ArrayList<>();
	protected List<List<JsonNode>> clusterContents = new ArrayList<>();
	protected Map<JsonNode, List<JsonNode>> repJsonMap = new HashMap<>();

	private String clustringStrategy;
	private String distanceAlgorithm;
	private int clusterTopN;
	private int clusterNum;
	private double exDistanceThreshold;
	private double clusterDistance;

	/**
	 * 
	 * @param strategy
	 * @param distanceAlgorithm
	 * @param topN
	 * @param clusterNum
	 * @param exDistanceThreshold
	 * @param clusterDistance
	 * @param nodes
	 */
	public Clusters(String strategy, String distanceAlgorithm, int topN, int clusterNum, double exDistanceThreshold,
			double clusterDistance, ArrayList<JsonNode> nodes) {
		this.clustringStrategy = strategy;
		this.distanceAlgorithm = distanceAlgorithm;
		this.clusterTopN = topN;
		this.exDistanceThreshold = exDistanceThreshold;
		this.clusterNum = clusterNum;
		this.clusterDistance = clusterDistance;
		this.allNode = nodes;
		clusteringNode();
	}

	public List<JsonNode> getAllNode() {
		return allNode;
	}

	public int getNodeSize() {
		return allNode.size();
	}

	public List<List<JsonNode>> getClusterReps() {
		return clusterReps;
	}

	public List<List<JsonNode>> getClusterContents() {
		return clusterContents;
	}

	public int getClusterRepsSize() {
		return clusterReps.size();
	}

	public Map<JsonNode, List<JsonNode>> getRepJsonMap() {
		return repJsonMap;
	}

	public void putRepJsonMap(JsonNode node, List<JsonNode> list) {
		repJsonMap.put(node, list);
	}

	/**
	 * Extract the topN nodes from nodes (sorted by distance) 
	 * @param topN
	 * @param nodes
	 * @return
	 */
	public static List<JsonNode> getTopNodes(int topN, List<JsonNode> nodes) {
		List<JsonNode> sortedList = getSortedListbyDistance(nodes);
		if (sortedList.size() <= topN) {
			return sortedList;
		} else {
			List<JsonNode> list = new ArrayList<>(sortedList);
			return list.subList(0, topN);
		}
	}

	private void clusteringNode() {
		clusteringInternal();
		for (List<JsonNode> nodes : clusterContents) {
			List<JsonNode> reps = getTopNodes(clusterTopN, nodes);
			clusterReps.add(reps);
			for (JsonNode node : reps) {
				repJsonMap.put(node, nodes);
			}
		}
		Collections.sort(clusterReps,
				(p1, p2) -> Double.compare(p1.get(0).get("Distance").asDouble(), p2.get(0).get("Distance").asDouble()));

	}

	private void clusteringInternal() {
		Clustering c;
		if (clustringStrategy.startsWith("EX")) {
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
			clusterContents = c.exClustering();
		} else if (clustringStrategy.startsWith("RM")) {
			if (clustringStrategy.startsWith("RMEXDFT")) {
				c = new RemoveDistanceTopFiltering(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold,
						clusterDistance);
			} else {
				/* RMEXDFD */
				c = new RemoveDistanceDisFiltering(allNode, distanceAlgorithm, clusterNum, exDistanceThreshold,
						clusterDistance);
			}
			clusterContents = c.clustering();
		} else {
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
			clusterContents = c.clustering();
		}
	}
	
	public ArrayList<JsonNode> getFilteredNodes(int allTopN, int clusterTopN) {
		ArrayList<JsonNode> selected = new ArrayList<>();
		for (List<JsonNode> nodes : getClusterReps()) {
			List<JsonNode> sortedNodes = getSortedListbyDistance(nodes);
			if (containTopN(nodes, getAllNode(), allTopN)) {
				List<JsonNode> list = new ArrayList<>(getRepJsonMap().get(sortedNodes.get(0)));
				selected.addAll(list.subList(0, Math.min(list.size(), clusterTopN)));
			}
		}
		return selected;
	}
	
	/*ascending order*/
	public static List<JsonNode> getSortedListbyDistance(List<JsonNode> nodes) {
		Collections.sort(nodes,
				(p1, p2) -> Double.compare(p1.get("Distance").asDouble(), p2.get("Distance").asDouble()));
		return nodes;
	}

	private boolean containTopN(List<JsonNode> nodes, List<JsonNode> allNode, int allTopN) {
		for (int i = 0; i < allTopN; i++) {
			JsonNode minNode = getSortedListbyDistance(allNode).get(i);
			if (nodes.contains(minNode)) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<JsonNode> getRemovedFilteredNodes(int allTopN, int clusterTopN) {
		ArrayList<JsonNode> selected = new ArrayList<>();
		for (List<JsonNode> nodes : getClusterReps()) {
			List<JsonNode> sortedNodes = getSortedListbyDistance(nodes);
			if (!containNontopN(nodes, getAllNode(), allTopN)) {

				List<JsonNode> list = new ArrayList<>(getRepJsonMap().get(sortedNodes.get(0)));
				selected.addAll(list.subList(0, Math.min(list.size(), clusterTopN)));
			}
		}
		return selected;
	}


	private boolean containNontopN(List<JsonNode> nodes, List<JsonNode> allNode, int allTopN) {
		for (int i = allNode.size() - 1; i >= allNode.size() - allTopN; i--) {
			JsonNode maxNode = getSortedListbyDistance(allNode).get(i);
			if (nodes.contains(maxNode)) {
				return true;
			}
		}
		return false;
	}

}
