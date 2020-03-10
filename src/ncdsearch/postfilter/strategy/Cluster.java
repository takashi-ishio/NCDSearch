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

	public Cluster() {
		components = new ArrayList<>();
	}
	public Cluster(Component node) {
		components = new ArrayList<>();
		components.add(node);
	}

	public Cluster(Cluster cluster1, Cluster cluster2) {
		components = new ArrayList<>();
		this.addAll(cluster1.components);
		this.addAll(cluster2.components);
	}

	public void addComponent(Component fragment) {
		components.add(fragment);
	}

	public void addAll(List<Component> fragments) {
		components.addAll(fragments);
	}

	public double getSumEdges(Cluster another) {
		double sumEdges = 0.0;
		for(Component f : components) {
			for(Component a : another.components) {
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