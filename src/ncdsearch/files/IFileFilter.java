package ncdsearch.files;

import java.io.File;

/**
 * An interface to select source files
 */
public interface IFileFilter {

	/**
	 * A filter method to check whether a file should be analyzed or not 
	 * @param f is a file found during a traversal 
	 * @return This method should return true if the file should be analyzed. 
	 */
	public boolean isTarget(File f);
	
}
