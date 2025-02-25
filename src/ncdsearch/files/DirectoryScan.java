package ncdsearch.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A utility class to traverse files in a directory. 
 */
public class DirectoryScan implements IFiles {
	
	private ArrayList<File> files = new ArrayList<File>(100);
	private IFileFilter filter;

	/**
	 * Construct an instance without file filtering.
	 * @param dirs specify a list of directories. 
	 */
	public DirectoryScan(List<String> dirs) {
		this(dirs, new IFileFilter() {
			@Override
			public boolean isTarget(File f) {
				return true;
			}
		});
	}

	/**
	 * Construct an instance with a file filter.
	 * @param dirs specify a list of directories. 
	 * @param filter is to select target files in the directories.
	 */
	public DirectoryScan(List<String> dirs, IFileFilter filter) {
		for (String d: dirs) {
			files.add(new File(d));
		}
		this.filter = filter;
	}

	/**
	 * Search all files in a directory recursively.
	 * @param dir specifies a search root.
	 * @param action is called for each file.
	 * @return the next file.  If the traversal completed, this method returns null.
	 */
	public IFile next() {
		while (!files.isEmpty()) {
			File f = files.remove(files.size()-1);
			if (f.isFile() && f.canRead() && filter.isTarget(f)) {
				return new SimpleFile(f);
			} else if (f.isDirectory() && f.canRead()) {
				File[] children = f.listFiles();
				if (children != null) {
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
		}
		return null;
	}

	@Override
	public void close() {
	}
}
