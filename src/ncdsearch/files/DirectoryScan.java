package ncdsearch.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A utility class to traverse files in a directory. 
 */
public class DirectoryScan implements IFiles {
	
	private ArrayList<File> files = new ArrayList<File>(100);
	
	public DirectoryScan(List<String> dirs) {
		for (String d: dirs) {
			files.add(new File(d));
		}
	}

	/**
	 * Search all files in a directory recursively.
	 * @param dir specifies a search root.
	 * @param action is called for each file.
	 */
	public File next() {
		while (!files.isEmpty()) {
			File f = files.remove(files.size()-1);
			if (f.isFile() && f.canRead()) {
				return f;
			} else if (f.isDirectory() && f.canRead()) {
				File[] children = f.listFiles();
				for (File c: children) {
					if (c.isFile() || 
						(c.isDirectory() &&
						 !c.getName().equals(".") && 
						 !c.getName().equals(".."))) {
						files.add(c);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void close() {
	}
}
