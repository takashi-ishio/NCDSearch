package ncdsearch.postfilter.strategy;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import gnu.trove.map.hash.TIntDoubleHashMap;

public class GroupAverage extends DistanceClustering {
	public GroupAverage(List<JsonNode> allNode, String strategy, int clusterNum) {
		super(allNode, strategy, clusterNum);
	}

	public GroupAverage(List<JsonNode> allNode, String strategy, int clusterNum, double exDistanceThreshold) {
		super(allNode, strategy, clusterNum, exDistanceThreshold);
	}

	@Override
	protected double calcDistance(Cluster c1, Cluster c2) {
		return c1.getMinDistance(c2);
	}

	@Override
	protected void update() {
		//			System.err.println("[UPDATE]");
		double minDistance = Double.MAX_VALUE;
		int minI = -1;

		/*get the most minimum node-node from minD map*/
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i]) {
				double distance = minDistanceMap.get(i);
				if (distance < minDistance) {
					minDistance = distance;
					minI = i;
				}
			}
		}
		int minJ = -1;
		TIntDoubleHashMap iMap = distanceMap.get(minI);

		for (int j = 0; j < totalVertexNumber; j++) {
			if (!removedFlagMap[j] && j != minI && minDistance == iMap.get(j)) {
				minJ = j;
			}
		}
		TIntDoubleHashMap jMap = distanceMap.get(minJ);
		for (int k = 0; k < totalVertexNumber; k++) {
			if (!removedFlagMap[k] && k != minI && k != minJ) {
				jMap.put(k, (iMap.get(k) + jMap.get(k)) / 2);
			}
		}
		/*remove clusterI and combine I to J as J*/
		distanceMap.put(minJ, jMap);
		clusterMap.get(minJ).combine(clusterMap.get(minI));
		removedFlagMap[minI] = true;

		setMinDistance();
	}
}
