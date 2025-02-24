package ncdsearch.comparison;

/**
 * StrategyManager uses this factory object to create strategy objects.
 */
public interface ICodeDistanceStrategyFactory {

	/**
	 * A factory method to create an instance of a strategy.
	 * @return a strategy object
	 */
	public ICodeDistanceStrategy create();
}
