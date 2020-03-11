package ncdsearch.postfilter.strategy;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the cluster of graph.
 *
 * @author ito-k
 *
 */
public class Cluster {
	private List<Component> components;

	/**
	 * Create a single-component cluster
	 * @param node
	 */
	public Cluster(Component node) {
		components = new ArrayList<>();
		components.add(node);
	}

	/**
	 * Merge two clusters
	 * @param cluster1
	 * @param cluster2
	 */
	public Cluster(Cluster cluster1, Cluster cluster2) {
		components = new ArrayList<>();
		components.addAll(cluster1.components);
		components.addAll(cluster2.components);
	}

	public double getSumEdges(Cluster another) {
		double sumEdges = 0.0;
		for(Component f: components) {
			for(Component a: another.components) {
				sumEdges += 1 / f.computeDistance(a);
			}
		}

		return sumEdges;
	}

	public double getMinDistance(Cluster another) {
		double minDistance = Double.MAX_VALUE;
		for(Component f : components) {
			for(Component a : another.components) {
				//System.err.println(f.getFragment().toTokensString() +", "+a.getFragment().toTokensString() +", "+f.computeDistance(a) );
				if(f.computeDistance(a) < minDistance)
					minDistance = f.computeDistance(a);
			}
		}
		return minDistance;
	}

	public double getMaxDistance(Cluster another) {
		double maxDistance = -Double.MAX_VALUE + 1;
		for(Component f : components) {
			for(Component a : another.components) {
				if(f.computeDistance(a) > maxDistance)
					maxDistance = f.computeDistance(a);
			}
		}
		return maxDistance;
	}


	/**
	 * Merge another cluster to this cluster. 
	 * This method update the cluster object.
	 * @param another
	 */
	public void combine(Cluster another) {
		components.addAll(another.components);
	}

	public List<Component> getComponents() {
		return components;
	}

//	public Component getDelegateComponent() {
//		return components.stream().min(Comparator.comparing(component -> component.getNode().getDistance())).get();
//	}

	public int size() {
		return components.size();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Cluster)) return false;
		Cluster another = (Cluster) o;
		if(components.containsAll(another.components) && another.components.containsAll(components)) return true;
		return false;
	}
}