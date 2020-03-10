package ncdsearch.postfilter.debug;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.JsonNodeInfo;
import ncdsearch.postfilter.strategy.Cluster;
import ncdsearch.postfilter.strategy.Component;

public class RemoveDistanceTopFiltering extends RemoveDistanceFiltering{
	public RemoveDistanceTopFiltering(List<JsonNode> allNode, String strategy, int clusterNum, double exDistanceThreshold,
			double clusterDistance) {
		super(allNode, strategy, clusterNum, exDistanceThreshold, clusterDistance);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected void addJsonNode(List<Cluster> allClusterList) {
		int topN = (int) exDistanceThreshold;
		for (int i = 0; i < totalVertexNumber; i++) {
			for (Component co : clusterMap.get(i).getComponents()) {
				if (allClusterList.size() == 0) {
					allClusterList.add(clusterMap.get(i));
					removedFlagMap[i] = true;
				} else {
					double distance = JsonNodeInfo.getNodeDistance(co.getJsonNode());
					boolean nonAdded = true;
					for (int j = 0; j < allClusterList.size() && j < topN && nonAdded; j++) {
						for (Component c : allClusterList.get(j).getComponents()) {
							if (distance > JsonNodeInfo.getNodeDistance(c.getJsonNode())) {
								allClusterList.add(j, clusterMap.get(i));
								removedFlagMap[i] = true;
								nonAdded = false;
							}
						}
					}
					if (allClusterList.size() > topN) {
						allClusterList.remove(topN);
					}
				}
			}
		}
	}
}
