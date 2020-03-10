package ncdsearch.postfilter.strategy;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import gnu.trove.map.hash.TIntDoubleHashMap;

public class Average extends DistanceClustering {

	public Average(List<JsonNode> allNode, String strategy, int clusterNum) {
		super(allNode, strategy, clusterNum);
	}

	public Average(List<JsonNode> allNode, String strategy, int clusterNum, double exDistanceThreshold) {
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
				int iElemSize = clusterMap.get(minI).size();
				int jElemSize = clusterMap.get(minJ).size();
				double averageDistance = (iMap.get(k) * iElemSize + jMap.get(k) * jElemSize) / (iElemSize + jElemSize);
				jMap.put(k, averageDistance);
			}
		}
		/*remove clusterI and combine I to J as J*/
		distanceMap.put(minJ, jMap);
		clusterMap.get(minJ).combine(clusterMap.get(minI));
		removedFlagMap[minI] = true;

		setMinDistance();
	}
}
