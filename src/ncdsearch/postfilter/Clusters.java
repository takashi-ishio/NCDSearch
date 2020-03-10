package ncdsearch.postfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.strategy.Clustering;


public class Clusters {

	protected List<JsonNode> allNode;
	protected List<List<JsonNode>> clusterReps = new ArrayList<>();
	protected List<List<JsonNode>> clusterContents = new ArrayList<>();

	/**
	 * 
	 * @param strategy
	 * @param distanceAlgorithm
	 * @param topN
	 * @param clusterNum
	 * @param exDistanceThreshold
	 * @param clusterDistance
	 * @param nodes This list must be sorted by distance.
	 */
	public Clusters(Clustering c, int representativeSize, ArrayList<JsonNode> nodes) {
		this.allNode = nodes;
		this.clusterContents = c.clustering();
		
		// Sort cluster contents
		for (int i=0; i<clusterContents.size(); i++) {
			sortByDistance(clusterContents.get(i));
		}
		// Sort clusters themsevles
		Collections.sort(clusterContents,
				(p1, p2) -> Double.compare(p1.get(0).get("Distance").asDouble(), p2.get(0).get("Distance").asDouble()));

		// Extract top-N nodes as representatives for each cluster
		for (List<JsonNode> cluster: clusterContents) {
			List<JsonNode> reps = getTopNodes(representativeSize, cluster);
			clusterReps.add(reps);
		}
	}

	public List<List<JsonNode>> getClusterContents() {
		return clusterContents;
	}

	public int getClusterCount() {
		return clusterReps.size();
	}

	/**
	 * Extract the topN nodes from nodes (sorted by distance) 
	 * @param topN
	 * @param nodes
	 * @return
	 */
	public static List<JsonNode> getTopNodes(int topN, List<JsonNode> nodes) {
		if (nodes.size() <= topN) {
			return nodes;
		} else {
			return nodes.subList(0, topN);
		}
	}

	
	/**
	 * Select top-k elements in clusters including at least one Top-N 
	 * nodes as representatives 
	 * @param allTopN
	 * @param clusterTopK Select top-k from a cluster
	 * @return
	 */
	public Set<JsonNode> getFilteredNodes(int allTopN, int clusterTopK) {
		Set<JsonNode> selected = Collections.newSetFromMap(new IdentityHashMap<JsonNode,Boolean>());
		for (int i=0; i<clusterReps.size(); i++) {
			List<JsonNode> representatives = clusterReps.get(i);
			if (containTopN(representatives, allTopN)) {
				List<JsonNode> list = clusterContents.get(i);
				selected.addAll(list.subList(0, Math.min(list.size(), clusterTopK)));
			}
		}
		return selected;
	}
	
	/**
	 * Sort by distance in the ascending order
	 * @param nodes
	 */
	public static void sortByDistance(List<JsonNode> nodes) {
		Collections.sort(nodes,
				(p1, p2) -> Double.compare(p1.get("Distance").asDouble(), p2.get("Distance").asDouble()));
	}

	/**
	 * Check if nodes include one of top-n elements
	 * @param nodes
	 * @param topN
	 * @return true if nodes include at least one Top-N elements in the entire ranking
	 */
	private boolean containTopN(List<JsonNode> nodes, int topN) {
		for (int i=0; i<topN; i++) {
			JsonNode minNode = allNode.get(i);
			if (nodes.contains(minNode)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Select top-k elements in clusters whose all representative 
	 * elements are Top-N nodes (i.e. this filter removes clusters 
	 * whose representatives are mixed with non-top-N elements,
	 * even if the element is in Top-N)
	 * @param topN
	 * @param clusterTopK
	 * @return
	 */
	public Set<JsonNode> getRemovedFilteredNodes(int topN, int clusterTopK) {
		Set<JsonNode> selected = Collections.newSetFromMap(new IdentityHashMap<JsonNode,Boolean>());
		for (int i=0; i<clusterReps.size(); i++) {
			List<JsonNode> representatives = clusterReps.get(i);
			if (!containNontopN(representatives, topN)) {
				List<JsonNode> list = clusterContents.get(i);
				selected.addAll(list.subList(0, Math.min(list.size(), clusterTopK)));
			}
		}
		return selected;
	}


	private boolean containNontopN(List<JsonNode> nodes, int topN) {
		for (int i=topN; i<allNode.size(); i++) {
			JsonNode nonTopNode = allNode.get(i);
			if (nodes.contains(nonTopNode)) {
				return true;
			}
		}
		return false;
	}

}
