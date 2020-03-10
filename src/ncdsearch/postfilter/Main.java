package ncdsearch.postfilter;

import java.io.File;
import java.nio.file.Paths;

import ncdsearch.postfilter.evaluate.DistanceFilteringEvaluate;
import ncdsearch.postfilter.evaluate.Evaluate;
import ncdsearch.postfilter.evaluate.IdealEvaluate;

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
	//	private static String distanceAlgorithm = "DIR";
	//	private static final int TOPN = 1100;
	/*when manually decided*/
	private static final int CLUSTER_NUM = 5;

	public static void main(String[] args) {
		//		try {
		//			BufferedWriter bw = Files.newBufferedWriter(Paths.get("F://ncdsearch/rereresult.txt"));
		//			bw.close();
		//		} catch (IOException e) {
		//			// TODO 自動生成された catch ブロック
		//			e.printStackTrace();
		//		}
		//String ID= args[1];
		//String ID = "4";
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
			callDistanceFilteringEvaluate(args[0], isRemoveClustering);
			//	distanceStart(args[0], isRemoveClustering);
		} else {
			//	callEvaluate(args[0], isRemoveClustering);
			start(args[0], isRemoveClustering);
		}
		//callIdealEvaluate(args[0]);
	}

	private static void start(String path, boolean isRemoveClustering) {
		Filtering f = new Filtering(checkN, CLUSTERTOPN, isRemoveClustering);
		filtering(path, f);
		//e.printAverage();
		//	printLogs(f);
	}

	//	private static void distanceStart(String path, boolean isRemoveClustering) {
	//		Evaluate e = new Evaluate(checkN, CLUSTERTOPN, isRemoveClustering);
	//		evaluate(path, e);
	//		//e.printAverage();
	//		printLogs(e);
	//	}

	private static void filtering(String path, Filtering f) {
		String inputJson;
		if (path.endsWith(".json")) {
			inputJson = path;
		} else {
			/*評価実験用*/
			String jsonPath = distanceAlgorithm.equals("ncd") ? "zip" : distanceAlgorithm;
			inputJson = Paths.get(path, ("result/" + jsonPath + "-0.5-fast-k0-29.json")).toAbsolutePath().toString();
		}

		InitJson ij = new InitJson(clusteringStrategy, distanceAlgorithm, REPN, CLUSTER_NUM, exDistanceThreshold,
				clusterDistance);
		Clusters cs = ij.converttoClusters(new File(inputJson));
		Clusters fcs = f.getFilteredClusters(cs);
		OutputResult outres = new OutputResult(cs, fcs, path);
		outres.print();
	}

	private static void callEvaluate(String path, boolean isRemoveClustering) {
		Evaluate e = new Evaluate(checkN, CLUSTERTOPN, isRemoveClustering);
		evaluate(path, e);
		//e.printAverage();
		printLogs(e);
	}

	private static void callIdealEvaluate(String path, boolean isRemoveClustering) {
		IdealEvaluate e = new IdealEvaluate(checkN, CLUSTERTOPN, isRemoveClustering);
		evaluate(path, e);
		printLogs(e);
	}

	private static void callDistanceFilteringEvaluate(String path, boolean isRemoveClustering) {
		DistanceFilteringEvaluate e = new DistanceFilteringEvaluate(checkN, CLUSTERTOPN, isRemoveClustering);
		evaluate(path, e);
		printLogs(e);
	}

	private static void evaluate(String path, Evaluate e) {
		for (int ID = 1; ID <= 53; ID++) {
			System.out.println("------------------");
			System.out.println("ID:" + ID);
			String answerJson = Paths.get(path, ("queries.json")).toAbsolutePath().toString();
			String jsonPath = distanceAlgorithm.equals("ncd") ? "zip" : distanceAlgorithm;
			String inputJson = Paths.get(path, ("result/" + jsonPath + "-0.5-fast-k0-" + ID + ".json")).toAbsolutePath()
					//String inputJson = Paths.get(path, ("result/lzjd-0.5-fast-k0-" + ID + ".json")).toAbsolutePath()
					.toString();
			InitJson ij = new InitJson(clusteringStrategy, distanceAlgorithm, REPN, CLUSTER_NUM, exDistanceThreshold,
					clusterDistance);
			Clusters cs = ij.converttoClusters(new File(inputJson));
			Answers a = ij.converttoAnswer(new File(answerJson), String.valueOf(ID));

			//output(cs);
			//e.setTopN(topNList[ID - 1]);
			//if(cs.getNodeSize()>10)
			e.evaluate(cs, a, path);
		}
	}

	private static void evaluate(String path, DistanceFilteringEvaluate e) {
		for (int ID = 1; ID <= 53; ID++) {
			System.out.println("------------------");
			System.out.println("ID:" + ID);
			String answerJson = Paths.get(path, ("queries.json")).toAbsolutePath().toString();
			String jsonPath = distanceAlgorithm.equals("ncd") ? "zip" : distanceAlgorithm;
			String inputJson = Paths.get(path, ("result/" + jsonPath + "-0.5-fast-k0-" + ID + ".json")).toAbsolutePath()
					//String inputJson = Paths.get(path, ("result/lzjd-0.5-fast-k0-" + ID + ".json")).toAbsolutePath()
					.toString();
			InitJson ij = new InitJson("NO", distanceAlgorithm, REPN, CLUSTER_NUM, 0, 0);
			Clusters cs = ij.converttoClusters(new File(inputJson));
			Answers a = ij.converttoAnswer(new File(answerJson), String.valueOf(ID));

			InitJson fij = new InitJson(clusteringStrategy, distanceAlgorithm, REPN, CLUSTER_NUM, exDistanceThreshold,
					clusterDistance);
			Clusters fcs = fij.converttoClusters(new File(inputJson));

			//output(cs);
			//e.setTopN(topNList[ID - 1]);
			//if(cs.getNodeSize()>10)
			e.evaluate(fcs, cs, a);
		}
	}

	private static void evaluate(String path, IdealEvaluate e) {
		for (int ID = 1; ID <= 53; ID++) {
			System.out.println("------------------");
			System.out.println("ID:" + ID);
			String answerJson = Paths.get(path, ("queries.json")).toAbsolutePath().toString();
			String jsonPath = distanceAlgorithm.equals("ncd") ? "zip" : distanceAlgorithm;
			String inputJson = Paths.get(path, ("result/" + jsonPath + "-0.5-fast-k0-" + ID + ".json")).toAbsolutePath()
					.toString();
			InitJson ij = new InitJson(clusteringStrategy, distanceAlgorithm, REPN, CLUSTER_NUM, exDistanceThreshold,
					clusterDistance);
			Clusters cs = ij.converttoClusters(new File(inputJson));
			Answers a = ij.converttoAnswer(new File(answerJson), String.valueOf(ID));

			e.evaluate(cs, a, path);
		}
	}

	//	private static void output(Clusters cs) {
	//		OutputClusters o = new OutputClusters(cs);
	//		//o.output();
	//		o.outputSorted();
	//	}
	//
	private static void printLogs(Evaluate e) {
		System.out.println("------------------");
		System.out.println("Total:");
		e.getData().printAll();
		;
		//		System.err.println(distanceAlgorithm + ", " + clusteringStrategy + ", Dis" + exDistanceThreshold + ", " + clusterDistance);
		System.err.println(distanceAlgorithm + ", " + clusteringStrategy + ", " + checkN + ", " + exDistanceThreshold
				+ ", " + clusterDistance);
		System.err.println("------------------");

		e.getData().printAverage();
	}
}
