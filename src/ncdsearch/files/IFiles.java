package ncdsearch.files;

import java.io.Closeable;
import java.io.File;

/**
 * The class represents a list of files.
 * This object should be closed after use 
 * so that the object can release allocated resource. 
 */
public interface IFiles extends Closeable {

	/**
	 * @return a file to be processed. 
	 * This method returns null if all files have been processed.  
	 */
	public File next();
	
}
