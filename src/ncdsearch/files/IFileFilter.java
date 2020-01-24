package ncdsearch.files;

import java.io.File;

public interface IFileFilter {

	public boolean isTarget(File f);
	
}
