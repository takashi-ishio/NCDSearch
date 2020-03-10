package ncdsearch.postfilter.debug;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.JsonNodeInfo;
import ncdsearch.postfilter.strategy.Cluster;
import ncdsearch.postfilter.strategy.Component;

public class RemoveDistanceDisFiltering extends RemoveDistanceFiltering {
	public RemoveDistanceDisFiltering(List<JsonNode> allNode, String strategy, int clusterNum,
			double exDistanceThreshold,
			double clusterDistance) {
		super(allNode, strategy, clusterNum, exDistanceThreshold, clusterDistance);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected void addJsonNode(List<Cluster> allClusterList) {
		for (int i = 0; i < totalVertexNumber; i++) {
			for (Component co : clusterMap.get(i).getComponents()) {
				if (JsonNodeInfo.getNodeDistance(co.getJsonNode()) > exDistanceThreshold) {
					allClusterList.add(clusterMap.get(i));
					removedFlagMap[i] = true;
				}
			}
		}
	}
}
