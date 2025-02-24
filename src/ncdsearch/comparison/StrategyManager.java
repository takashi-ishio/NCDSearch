package ncdsearch.comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This object keeps distance strategy objects for each thread
 * so that a thread can reuse a single instance for multiple files.
 * This is introduced because a constructor call may be expensive. 
 */
public class StrategyManager implements AutoCloseable {

	/** 
	 * This is a List to record all strategy objects created for multi-threaded search.
	 * This object must be a synchronized collection because multiple threads 
	 * may create new strategy objects in parallel.   
	 */
	private List<ICodeDistanceStrategy> createdStrategyInstances = 
			Collections.synchronizedList(new ArrayList<ICodeDistanceStrategy>());

	/** 
	 * A search strategy object is created for each thread
	 */
	private ThreadLocal<ICodeDistanceStrategy> instances;

	/**
	 * The constructor to initialize the manager.
	 * @param factory specifies an object creating strategy objects.
	 */
	public StrategyManager(ICodeDistanceStrategyFactory factory) {
		instances = new ThreadLocal<ICodeDistanceStrategy>() {
			@Override
			protected ICodeDistanceStrategy initialValue() {
				ICodeDistanceStrategy similarityStrategy = factory.create();
				createdStrategyInstances.add(similarityStrategy);
				return similarityStrategy;
			}
		};
	}
	
	/**
	 * @return a thread-local instance of a strategy object.
	 */
	public ICodeDistanceStrategy getThreadLocalInstance() {
		return instances.get();
	}
	
	/**
	 * This method releases all strategy instances.
	 */
	public void close() {
		for (ICodeDistanceStrategy s: createdStrategyInstances) {
			s.close();
		}
	}
	
}
