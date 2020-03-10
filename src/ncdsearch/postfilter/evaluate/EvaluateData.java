package ncdsearch.postfilter.evaluate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import ncdsearch.postfilter.Answers;
import ncdsearch.postfilter.Clusters;

public class EvaluateData {
	public List<Double> reduceWorks = new ArrayList<>();
	public List<Double> precisions = new ArrayList<>();
	public List<Double> recalls = new ArrayList<>();
	public List<Double> fvalues = new ArrayList<>();
	public List<Integer> fcsNodeSizes = new ArrayList<>();
	public int totalCall = 0;
	public int totalNan = 0;
	public int totalResultNode = 0;
	public int totalFilteredNode = 0;
	public int totalAnswerNode = 0;
	public int totalPFind = 0;
	public int totalPAll = 0;
	public int totalRFind = 0;
	public int totalRAll = 0;

	public void printAll() {
		System.out.println("--------------");
		System.out.println("Reduce Works");
		Collections.sort(reduceWorks);
		reduceWorks.forEach(s -> System.out.println(s));

		System.out.println("--------------");
		System.out.println("Precision");
		Collections.sort(precisions);
		precisions.forEach(s -> System.out.println(s));

		System.out.println("--------------");
		System.out.println("Recall");
		Collections.sort(recalls);
		recalls.forEach(s -> System.out.println(s));

		System.out.println("--------------");
		System.out.println("Filtered Node Size");
		fcsNodeSizes.forEach(s -> System.out.println(s));

		//		System.out.println("--------------");
		//		System.out.println("F-Value");
		//		Collections.sort(fvalues);
		//		fvalues.forEach(s -> System.out.println(s));

	}

	public void printAverage() {
		double sum = 0.0;
		for (double d : reduceWorks) {
			sum += d;
		}
		//System.err.println("Ave Reduction rate: " + sum / totalCall);
		System.err.println(sum / totalCall);
		sum = 0.0;
		for (double d : precisions) {
			sum += d;
		}
		//System.err.println("Ave Precision: " + sum / (totalCall - totalNan));
		System.err.println(sum / (totalCall - totalNan));
		sum = 0.0;
		for (double d : recalls) {
			sum += d;
		}
		//System.err.println("Ave Recall: " + sum / totalCall);
		System.err.println(sum / totalCall);
		//		sum = 0.0;
		//		for (double d : fvalues) {
		//			sum += d;
		//		}
		//		System.out.println("Ave Fvalue: " + sum / totalCall);
		double precision = (double) totalPFind / totalPAll;
		double recall = (double) totalRFind / totalRAll;
		double reduction = 1.0 - (double) totalFilteredNode / totalResultNode;
		//		System.err.println("Total Reduction rate: " + reduction);
		//		System.err.println("Total Precision: " + precision);
		//		System.err.println("Total Recall: " + recall);
		//		System.err.println("Total F-value: " + 2 * precision * recall / (precision + recall));
		//		System.err.println("TotalCheckedNode: " + totalFilteredNode);
		//		System.err.println("TotalAnswerNode/TotalResultNode:" + totalAnswerNode + "/" + totalResultNode + ": "
		//				+ (double) totalAnswerNode / totalResultNode);
		System.err.println(reduction);
		System.err.println(precision);
		System.err.println(recall);
		System.err.println(2 * precision * recall / (precision + recall));
		System.err.println(totalFilteredNode);
		//		System.err.println(totalAnswerNode + "/" + totalResultNode + ": "
		//				+ (double) totalAnswerNode / totalResultNode);
	}

	public void calcReduceWork(Clusters cs, Clusters fcs, int nonAnswerRepSize) {
		System.out.println(cs.getNodeSize() + "+" + fcs.getNodeSize() + "+" + nonAnswerRepSize);
		totalFilteredNode += fcs.getNodeSize() + nonAnswerRepSize;
		double reduceWork = (double) (cs.getNodeSize() - fcs.getNodeSize() - nonAnswerRepSize) / cs.getNodeSize();
		reduceWorks.add(reduceWork);
		System.out.println("Reduction rate: " + reduceWork);
	}


	//TODO fix at denominator
	public void calcPrecision(Clusters fcs, Answers a) {
		int size = 0;
		for (JsonNode node : fcs.getAllNode()) {
			if (CompareNodes.isContainInAnswer(node, a.getAllNode())) {
				size++;
			}
		}
		System.out.println(size + "/" + fcs.getNodeSize());
		//System.out.println("Precision: " + (double) size);
		double precision = (double) size / fcs.getNodeSize();
		totalPFind += size;
		totalPAll += fcs.getNodeSize();
		if (fcs.getNodeSize() != 0) {
			precisions.add(precision);
			System.out.println("Precision: " + precision);
		} else {
			totalNan++;
			System.out.println("Precision: NAN");
		}
	}

	public void calcRecall(Clusters fcs, Answers a) {
		int size = 0;
		for (JsonNode aNode : a.getAllNode()) {
			if (CompareNodes.isContainInResult(aNode, fcs.getAllNode())) {
				size++;
			}
		}
		System.out.println(size + "/" + a.getAllNodeSize());
		totalRFind += size;
		totalRAll += a.getAllNodeSize();
		double recall = (double) size / a.getAllNodeSize();
		recalls.add(recall);
		System.out.println("Recall: " + recall);
	}

	public void calcFvalue() {
		double precision = precisions.get(totalCall - 1);
		double recall = recalls.get(totalCall - 1);
		double fvalue = 2 * precision * recall / (precision + recall);
		fvalues.add(fvalue);
		System.out.println("Fvalue: " + fvalue);
	}


}