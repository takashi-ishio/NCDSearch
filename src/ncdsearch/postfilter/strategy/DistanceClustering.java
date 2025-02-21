package ncdsearch.postfilter.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public abstract class DistanceClustering extends Clustering {
	
	private boolean DEBUG = false;
	
	protected TIntObjectHashMap<TIntDoubleHashMap> distanceMap;
	protected TIntDoubleHashMap minDistanceMap;
	protected TIntObjectHashMap<Cluster> clusterMap;

	protected int clusterNum;
	protected int totalVertexNumber;
	protected boolean[] removedFlagMap;
	protected double exDistanceThreshold;
	
	private boolean enableExClustering;

	public DistanceClustering(List<JsonNode> allNode, String strategy, int clusterNum) {
		super(allNode, strategy);
		this.clusterNum = clusterNum;
		this.enableExClustering = false;
	}

	public DistanceClustering(List<JsonNode> allNode, String strategy, int clusterNum, double exDistanceThreshold) {
		this(allNode, strategy, clusterNum);
		this.exDistanceThreshold = exDistanceThreshold;
		this.enableExClustering = true;
	}
	
	@Override
	public List<List<JsonNode>> clustering() {
		if (enableExClustering) {
			return exClustering();
		}
		init();
		int mapSize = totalVertexNumber;
		if (DEBUG) System.err.println("initial clusters : " + mapSize);
		int idx = 0;
		while (mapSize > clusterNum) {
			idx++;
			update();
			int count = 0;
			for (boolean flag : removedFlagMap) {
				if (!flag)
					count++;
			}
			mapSize = count;
		}

		if (DEBUG) System.err.println("iterate count : " + idx);

		return getNodeList();
	}

	public List<List<JsonNode>> exClustering() {
		init();
		int mapSize = totalVertexNumber;
		double minDistance = 0.0;
		if (DEBUG) System.err.println("initial clusters : " + mapSize);
		int idx = 1;
		while (minDistance <= exDistanceThreshold && idx != totalVertexNumber) {
			idx++;
			update();
			minDistance = getMinDistance();
		}

		if (DEBUG) System.err.println("iterate count : " + (idx - 1));
		return getNodeList();
	}

	protected void init() {
		distanceMap = new TIntObjectHashMap<>();
		minDistanceMap = new TIntDoubleHashMap();
		clusterMap = new TIntObjectHashMap<>();

		totalVertexNumber = allNode.size();
		//this.clusterNum = allNode.size() / 5 + 1;

		removedFlagMap = new boolean[totalVertexNumber];
		Arrays.fill(removedFlagMap, false);

		List<Component> components = new ArrayList<>();
		for (JsonNode node : allNode) {
			components.add(new Component(node, strategy));
		}
		createInitialClusters(components);
	}

	protected List<List<JsonNode>> getNodeList() {
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

	protected List<Cluster> createInitialClusters(List<Component> nodes) {
		List<Cluster> clusters = new ArrayList<>();
		int index = 0;
		for (Component node : nodes) {
			Cluster cluster = new Cluster(node);
			clusterMap.put(index++, cluster);
			clusters.add(cluster);
		}
		calcInitialDistances(clusters);
		return clusters;
	}

	protected void calcInitialDistances(List<Cluster> clusters) {
		int arraySize = clusters.size();
		for (int i = 0; i < arraySize; i++) {
			Cluster target = clusters.get(i);
			TIntDoubleHashMap map = new TIntDoubleHashMap();
			double min = Double.MAX_VALUE;
			for (int j = 0; j < arraySize; j++) {
				if (i < j) {
					Cluster cluster = clusters.get(j);
					double distance = calcDistance(target, cluster);
					map.put(j, distance);
					if (distance < min)
						min = distance;
					//							System.err.print(distance + ",");
				} else if (i > j) {
					double distance = distanceMap.get(j).get(i);
					map.put(j, distance);
					if (distance < min)
						min = distance;
					//							System.err.print(distance + ",");
				}
			}
			//				System.err.print("aaa ,");
			//				System.err.println();
			distanceMap.put(i, map);
			minDistanceMap.put(i, min);
		}
	}

	protected void setMinDistance() {
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i]) {
				double min = Double.MAX_VALUE;
				TIntDoubleHashMap innerMap = distanceMap.get(i);
				for (int j = 0; j < totalVertexNumber; j++) {
					if (i != j && !removedFlagMap[j]) {
						double distance = innerMap.get(j);
						if (distance < min) {
							min = distance;
						}
					}
				}
				minDistanceMap.put(i, min);
			}
		}
	}

	protected double getMinDistance() {
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i]) {
				double distance = minDistanceMap.get(i);
				if (distance < minDistance) {
					minDistance = distance;
				}
			}
		}
		return minDistance;
	}

	protected abstract void update();

	protected abstract double calcDistance(Cluster c1, Cluster c2);

}
