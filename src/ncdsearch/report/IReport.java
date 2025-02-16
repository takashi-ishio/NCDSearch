package ncdsearch.report;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * This interface defines methods to write a runtime configuration and 
 * execution results in a particular format. 
 */
public interface IReport extends Closeable {
	
	/**
	 * This method is called to write a configuration parameter.
	 * @param attrName is a parameter name.
	 * @param attrValue is a parameter value.
	 * @throws IOException
	 */
	public void writeConfig(String attrName, String attrValue) throws IOException;

	/**
	 * This method is called to write a list of code fragments 
	 * detected by the search engine.
	 * Implementation should be thread safe because multiple threads 
	 * would report code fragments in different files. 
	 * @param fragments is a list of detected code fragments.
	 * @throws IOException
	 */
	public void write(List<Fragment> fragments) throws IOException;
	
	/**
	 * 
	 * @param filename
	 * @param lines
	 * @param tokens
	 */
	public void recordAnalyzedFile(String filename, int lines, int tokens);
}
