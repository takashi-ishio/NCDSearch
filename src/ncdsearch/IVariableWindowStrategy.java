package ncdsearch;

public interface IVariableWindowStrategy extends ICodeDistanceStrategy {

	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold);
	public int getBestWindowSize();

}
