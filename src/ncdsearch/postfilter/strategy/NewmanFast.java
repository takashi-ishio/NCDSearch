package ncdsearch.postfilter.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class NewmanFast extends Clustering {
	private TIntObjectHashMap<TIntDoubleHashMap> deltaModularityMap;
	private TIntDoubleHashMap maxDeltaModularityMap;
	private TIntObjectHashMap<Cluster> clusterMap;
	private int totalVertexNumber;
	private int totalEdgeNumber;
	private boolean[] removedFlagMap;

	public NewmanFast(List<JsonNode> allNode, String strategy) {
		super(allNode, strategy);
		totalVertexNumber = 0;
		totalEdgeNumber = 0;
	}

	@Override
	public List<List<JsonNode>> clustering() {
		deltaModularityMap = new TIntObjectHashMap<>();
		maxDeltaModularityMap = new TIntDoubleHashMap();
		clusterMap = new TIntObjectHashMap<>();

		totalVertexNumber = allNode.size();
		totalEdgeNumber = totalVertexNumber - 1;
		removedFlagMap = new boolean[totalVertexNumber];
		Arrays.fill(removedFlagMap, false);

		List<Component> components = new ArrayList<>();
		for (JsonNode node : allNode) {
			components.add(new Component(node, strategy));
		}
		createInitialClusters(components);
		double maxDeltaModularity = -Double.MAX_VALUE + 1;
		int mapSize = totalVertexNumber;
		double beforeMax = -Double.MAX_VALUE;
		//		System.err.println("mapsize = " + mapSize + " diff = " + (beforeMax - maxDeltaModularity));
		System.err.println("initial clusters : " + mapSize);
		int idx = 0;
		while (mapSize > 1 && beforeMax - maxDeltaModularity <= 0) {
			idx++;
			beforeMax = maxDeltaModularity;
			maxDeltaModularity = update();
			int count = 0;
			for (boolean flag : removedFlagMap) {
				if (!flag)
					count++;
			}
			mapSize = count;
			//			System.err.println("before : " + beforeMax);
			//			System.err.println("current : " + maxDeltaModularity);
		}

		System.err.println("iterate count : " + idx);
		List<List<JsonNode>> nodeList = new ArrayList<>();
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i]) {
				Cluster c = clusterMap.get(i);
				List<JsonNode> list = new ArrayList<>();
				for (Component co : c.getComponents()) {
					list.add(co.getJsonNode());
				}
				nodeList.add(list);

			}
		}
		return nodeList;
	}

	@Override
	public List<List<JsonNode>> exClustering() {
		return clustering();
	}

	private List<Cluster> createInitialClusters(List<Component> fragments) {
		List<Cluster> clusters = new ArrayList<>();
		int index = 0;
		for (Component fragment : fragments) {
			Cluster cluster = new Cluster(fragment);
			clusterMap.put(index++, cluster);
			clusters.add(cluster);
		}
		calcInitialDeltaModularities(clusters);
		return clusters;
	}

	private void calcInitialDeltaModularities(List<Cluster> clusters) {
		int arraySize = clusters.size();
		for (int i = 0; i < arraySize; i++) {
			Cluster target = clusters.get(i);
			TIntDoubleHashMap map = new TIntDoubleHashMap();
			double max = -Double.MAX_VALUE;
			for (int j = 0; j < arraySize; j++) {
				if (i < j) {
					Cluster cluster = clusters.get(j);
					double deltaModularity = calcDeltaModularity(target, cluster);
					map.put(j, deltaModularity);
					if (max < deltaModularity)
						max = deltaModularity;
					//					System.err.print(deltaModularity + " ");
				} else if (i > j) {
					double deltaModularity = deltaModularityMap.get(j).get(i);
					map.put(j, deltaModularity);
					if (max < deltaModularity)
						max = deltaModularity;
					//					System.err.print(deltaModularity + " ");
				}
			}
			//			System.err.println();
			deltaModularityMap.put(i, map);
			maxDeltaModularityMap.put(i, max);
		}
	}

	private void setMaxDeltaModularity() {
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i]) {
				double max = -Double.MAX_VALUE;
				TIntDoubleHashMap innerMap = deltaModularityMap.get(i);
				for (int j = 0; j < totalVertexNumber; j++) {
					if (i != j && !removedFlagMap[j]) {
						double deltaModularity = innerMap.get(j);
						if (max < deltaModularity) {
							max = deltaModularity;
						}
					}
				}
				maxDeltaModularityMap.put(i, max);
			}
		}
	}

	private double update() {
		//		System.err.println("[UPDATE]");
		double maxDeltaModularity = -Double.MAX_VALUE;
		int maxI = -1;
		/*get max delta modularity cluster*/
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i]) {
				double deltaModularity = maxDeltaModularityMap.get(i);
				if (maxDeltaModularity < deltaModularity) {
					maxDeltaModularity = deltaModularity;
					maxI = i;
				}
			}
		}
		int maxJ = -1;
		TIntDoubleHashMap iMap = deltaModularityMap.get(maxI);
		/*get max delta modularity cluster pair*/
		for (int j = 0; j < totalVertexNumber; j++) {
			if (!removedFlagMap[j] && j != maxI && maxDeltaModularity == iMap.get(j)) {
				maxJ = j;
			}
		}
		TIntDoubleHashMap jMap = deltaModularityMap.get(maxJ);
		for (int k = 0; k < totalVertexNumber; k++) {
			if (!removedFlagMap[k] && k != maxI && k != maxJ) {
				//				System.err.println("maxi = " + maxI + " maxj = " + maxJ +" k = " + k);
				//				double ivalue = iMap.get(k);
				//				double jvalue = jMap.get(k);
				//				System.err.println("iMap.get(k) = " + ivalue + " jMap.get(k) = " + jvalue);
				jMap.put(k, iMap.get(k) + jMap.get(k));
			}
		}
		/*remove clusterI and combine I to J as J*/
		deltaModularityMap.put(maxJ, jMap);
		clusterMap.get(maxJ).combine(clusterMap.get(maxI));
		removedFlagMap[maxI] = true;
		//		System.err.println("before max map");
		//		for(int i = 0; i < totalEdgeNumber; i++) {
		//			System.err.print(maxDeltaModularityMap.get(i) + " ");
		//		}
		//		System.err.println();
		setMaxDeltaModularity();
		//		for(int i = 0; i < totalEdgeNumber; i++) {
		//			System.err.print(maxDeltaModularityMap.get(i) + " ");
		//		}
		//		System.err.println();
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i]) {
				double deltaModularity = maxDeltaModularityMap.get(i);
				if (maxDeltaModularity < deltaModularity) {
					maxDeltaModularity = deltaModularity;
				}
			}
		}
		return maxDeltaModularity;
	}

	private double calcDeltaModularity(Cluster c1, Cluster c2) {
		return (1 - c1.getSumEdges(c2) * c2.getSumEdges(c1) / (2 * totalEdgeNumber)) / (2 * totalEdgeNumber);
	}
}
