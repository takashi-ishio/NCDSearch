package ncdsearch;

public interface IFastDistanceStrategy extends ICodeDistanceStrategy {

	public double findBestMatch(TokenSequence code, int startPos, int endPos, double threshold);
	public int getBestWindowSize();

}
