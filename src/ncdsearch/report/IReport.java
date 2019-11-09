package ncdsearch.report;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import ncdsearch.Fragment;

public interface IReport extends Closeable {
	
	public void writeConfig(String attrName, String attrValue);

	/**
	 * Implementation should be thread safe because multiple threads 
	 * would report code fragments. 
	 * @param fragments
	 */
	public void write(List<Fragment> fragments) throws IOException;
}
