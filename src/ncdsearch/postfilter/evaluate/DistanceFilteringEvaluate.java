package ncdsearch.postfilter.evaluate;

import ncdsearch.postfilter.Answers;
import ncdsearch.postfilter.Clusters;

public class DistanceFilteringEvaluate extends Evaluate {

	public DistanceFilteringEvaluate(String checkN, int clusterTopN, boolean isRemoveClustering) {
		super(checkN, clusterTopN, isRemoveClustering);
	}


	public void evaluate(Clusters fcs, Clusters cs, Answers a) {
//		if (isDistance) {
//			setTopN(cs);
//		}


		System.out.println("Filtered Node: " + fcs.getNodeSize());
		System.out.println("Filtered Dir: " + fcs.getClusterRepsSize());

		pushToTotal(cs, a, fcs );
		printResult(cs, a, fcs);
	}
}
