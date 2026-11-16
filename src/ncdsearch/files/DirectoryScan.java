package ncdsearch.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 * A utility class to traverse files in a directory. 
 * The traversal uses file attributes obtained by Files.walkFileTree 
 * instead of checking each file by java.io.File methods, 
 * because the latter issues several system calls for each file. 
 * On Windows, Files.walkFileTree reuses attributes returned by a directory listing.
 */
public class DirectoryScan implements IFiles {
	
	/**
	 * A stack of files and directories to be visited
	 */
	private ArrayList<Entry> entries = new ArrayList<>(100);
	private IFileFilter filter;

	/**
	 * An element of the stack
	 */
	private static class Entry {
		private Path path;
		private boolean isDirectory;
		
		public Entry(Path path, boolean isDirectory) {
			this.path = path;
			this.isDirectory = isDirectory;
		}
	}

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
		this.filter = filter;
		for (String d: dirs) {
			try {
				Path p = Paths.get(d);
				if (Files.isDirectory(p)) {
					entries.add(new Entry(p, true));
				} else if (Files.isRegularFile(p)) {
					entries.add(new Entry(p, false));
				}
			} catch (InvalidPathException e) {
				// Ignore an invalid path as java.io.File does
			}
		}
	}

	/**
	 * Search all files in a directory recursively.
	 * Files in a directory are visited in the reverse order of the directory listing.
	 * @return the next file.  If the traversal completed, this method returns null.
	 */
	public IFile next() {
		while (!entries.isEmpty()) {
			Entry e = entries.remove(entries.size()-1);
			if (e.isDirectory) {
				listChildren(e.path);
			} else {
				File f = e.path.toFile();
				if (filter.isTarget(f)) {
					return new SimpleFile(f);
				}
			}
		}
		return null;
	}
	
	/**
	 * Push files and directories in a directory to the stack.
	 * Symbolic links are followed as java.io.File does.
	 * @param dir specifies a directory.
	 */
	private void listChildren(Path dir) {
		try {
			Files.walkFileTree(dir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					// With maxDepth=1, sub-directories are also reported by this method
					if (attrs.isDirectory()) {
						entries.add(new Entry(file, true));
					} else if (attrs.isRegularFile()) {
						entries.add(new Entry(file, false));
					}
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					// Skip a file or directory that cannot be accessed
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			// Skip the directory
		}
	}

	@Override
	public void close() {
	}
}
