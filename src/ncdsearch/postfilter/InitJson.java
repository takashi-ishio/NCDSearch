package ncdsearch.postfilter;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/*Json to List<JsonNode>*/
public class InitJson {
	private static final String KEY_RESULT = "Result";
	private static final String KEY_ANSWERS = "answers";
	//private static final String MODE = "DIR";
	private String clusteringStrategy;
	private String distanceAlgorithm;
	private int topN;
	private int clusterNum;
	private double exDistanceThreshold;
	private double clusterDistance;

	public InitJson(String clusteringStrategy, String distanceAlgorithm, int topN, int clusterNum,
			double exDistanceThreshold, double clusterDistance) {
		this.clusteringStrategy = clusteringStrategy;
		this.distanceAlgorithm = distanceAlgorithm;
		this.topN = topN;
		this.clusterNum = clusterNum;
		this.exDistanceThreshold = exDistanceThreshold;
		this.clusterDistance=clusterDistance;
	}

	public Clusters converttoClusters(File file) {
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

	public Answers converttoAnswer(File answer, String KEY_NUMBER) {
		Answers a = new Answers();
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(answer);
			for (JsonNode node : root.get(KEY_NUMBER).get(KEY_ANSWERS)) {
				a.addNode(node);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return a;
	}
}
