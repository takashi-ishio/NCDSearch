package ncdsearch.postfilter;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.evaluate.Filter;

public class Filtering {
	protected int clusterTopN;
	protected int allTopN;
	protected double distanceThreshold;
	protected boolean isRemoveClustering;
	protected boolean isDistance;

	public Filtering(String checkN, int clusterTopN, boolean isRemoveClustering) {
		this.allTopN = setAllTopN(checkN);
		this.clusterTopN = clusterTopN;
		this.isRemoveClustering = isRemoveClustering;
	}

	private int setAllTopN(String checkN) {
		if (checkN.startsWith("Top")) {
			return Integer.parseInt(checkN.substring("Top".length()));
		} else if (checkN.startsWith("Dis")) {
			this.isDistance = true;
			this.distanceThreshold = Double.parseDouble(checkN.substring("Dis".length()));
			return 1;
		} else {
			return 10;
		}
	}

	protected void setTopN(Clusters cs) {
		allTopN = 0;
		if (isRemoveClustering) {
			for (JsonNode node : cs.getAllNode()) {
				if (JsonNodeInfo.getNodeDistance(node) > distanceThreshold) {
					allTopN++;
				}
			}
		} else {
			for (JsonNode node : cs.getAllNode()) {
				if (JsonNodeInfo.getNodeDistance(node) <= distanceThreshold) {
					allTopN++;
				}
			}
		}
	}

	public Clusters getFilteredClusters(Clusters cs) {
		/*Distance to TopN*/
		if (isDistance) {
			setTopN(cs);
		}
		Filter f = new Filter(allTopN, clusterTopN, isRemoveClustering);
		if (isRemoveClustering) {
			return f.getRemovedFilteredClusters(cs);
		} else {
			return f.getFilteredClusters(cs);
		}
	}
}
