package ncdsearch.postfilter.debug;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import gnu.trove.map.hash.TIntDoubleHashMap;
import ncdsearch.postfilter.strategy.Cluster;
import ncdsearch.postfilter.strategy.Component;

public abstract class RemoveDistanceFiltering extends DistanceFiltering {
	protected abstract void addJsonNode(List<Cluster> allClusterList);

	public RemoveDistanceFiltering(List<JsonNode> allNode, String strategy, int clusterNum, double exDistanceThreshold,
			double clusterDistance) {
		super(allNode, strategy, clusterNum, exDistanceThreshold, clusterDistance);
	}

	@Override
	public List<List<JsonNode>> clustering() {
		init();
		/*add element whose distance is less than threshold*/
		List<Cluster> allClusterList = new ArrayList<>();

		/*TopN or DistanceN*/
		addJsonNode(allClusterList);

		/*add element near fragment selected previous block*/
		addNearElements(allClusterList);
		/*push element to filteredlist*/
		List<List<JsonNode>> nodeList = new ArrayList<>();
		this.allNode.clear();
		pushElements(nodeList);
		return nodeList;
	}

	@Override
	protected void addNearElements(List<Cluster> allClusterList) {
		boolean[] tmpRemovedFlagMap = new boolean[totalVertexNumber];
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i])
				continue;
			TIntDoubleHashMap innerMap = distanceMap.get(i);
			for (int j = 0; j < totalVertexNumber; j++) {
				if (i != j && !removedFlagMap[j]) {
					double distance = innerMap.get(j);
					//need additional param
					if (distance <= clusterDistance) {
						allClusterList.add(clusterMap.get(i));
						tmpRemovedFlagMap[j] = true;
					}
				}
			}
		}
		for (int i = 0; i < totalVertexNumber; i++) {
			removedFlagMap[i] |= tmpRemovedFlagMap[i];
		}
	}
	@Override
	protected void pushElements(List<List<JsonNode>> nodeList) {
		for (int i = 0; i < totalVertexNumber; i++) {
			if (!removedFlagMap[i]) {
				for (Component co : clusterMap.get(i).getComponents()) {
					this.allNode.add(co.getJsonNode());
					List<JsonNode> list = new ArrayList<>();
					list.add(co.getJsonNode());
					nodeList.add(list);
				}
			}
		}
	}
}
