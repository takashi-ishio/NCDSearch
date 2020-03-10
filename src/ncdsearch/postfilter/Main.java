package ncdsearch.postfilter;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Main {
	private static String clusteringStrategy = "EXGA";
	private static String distanceAlgorithm = "ncd";
	private static String checkN = "Dis0.1";
	private static double exDistanceThreshold = 0.35;
	/*optional*/
	private static double clusterDistance = 0;

	//Optional Param
	private static final int REPN = 10;
	private static final int CLUSTERTOPN = 10000;
	private static final int CLUSTER_NUM = 5;

	public static void main(String[] args) {
		
		File json = new File(args[0]);
		
		if (args.length > 1) {
			checkN = args[1];
			exDistanceThreshold = Double.parseDouble(args[2]);
			clusteringStrategy = args[3];
		}
		if (args.length > 4) {
			clusterDistance = Double.parseDouble(args[4]);

		}
		boolean isRemoveClustering;
		if (clusteringStrategy.startsWith("RM")) {
			isRemoveClustering = true;
			clusteringStrategy = clusteringStrategy.substring(2);
		} else {
			isRemoveClustering = false;
		}

		if (clusteringStrategy.startsWith("EXDF")) {
			if (isRemoveClustering)
				clusteringStrategy = "RM" + clusteringStrategy;
		}
		
		Filtering f = new Filtering(checkN, CLUSTERTOPN, isRemoveClustering);
		Clusters cs = converttoClusters(json, clusteringStrategy, distanceAlgorithm, REPN, CLUSTER_NUM, exDistanceThreshold, clusterDistance);
		Clusters fcs = f.getFilteredClusters(cs);
		OutputResult outres = new OutputResult(cs, fcs);
		outres.print();
	}

	private static final String KEY_RESULT = "Result";

	public static Clusters converttoClusters(File file, String clusteringStrategy, String distanceAlgorithm, int topN, int clusterNum,
			double exDistanceThreshold, double clusterDistance) {
		Clusters cs = new Clusters(clusteringStrategy, distanceAlgorithm, topN, clusterNum, exDistanceThreshold, clusterDistance);
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(file);
			for (JsonNode node : root.get(KEY_RESULT)) {
				cs.addNode(node);
			}
			cs.clusteringNode();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return cs;
	}

}
