package ncdsearch.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;


public class GitScan implements IFiles {
	
	
	private Git git;
	private Iterator<RevCommit> commits;
	private TreeWalk currentWalk;
	private HashSet<ObjectId> processed;
	private String currentCommitId; 
	
	private boolean previousCommits = false;
	
	/**
	 * Construct an instance without file filtering
	 * @param dirs
	 */
	public GitScan(File gitDir, String gitCommit) {
		if (gitCommit == null) gitCommit = "HEAD";
		processed = new HashSet<>(65536);
		
		try {
			git = Git.open(gitDir);
			AnyObjectId objId = git.getRepository().resolve(gitCommit);
			Iterable<RevCommit> c = git.log().add(objId).call();
			commits = c.iterator();
			if (commits.hasNext()) {
				RevCommit current = commits.next();
				RevTree tree = current.getTree();
				currentCommitId = current.getId().getName();
				currentWalk = new TreeWalk(git.getRepository());
				currentWalk.addTree(tree);
				currentWalk.setRecursive(true);
			}
		} catch (IOException e) {
			if (git != null) {
				git.close();
				git = null;
			}
		} catch (GitAPIException e) {
			git.close();
			git = null;
		}
	}
	
	@Override
	public IFile next() {
		while (currentWalk != null) {
			try {
				while (currentWalk.next()) {
					ObjectId id = currentWalk.getObjectId(0);
					if (processed.add(id)) {
						String path = new String(currentWalk.getRawPath());
						return new GitObject(id, currentCommitId + ";" + path);
					}
				}
				// If not found
				currentWalk.close();
				
				if (previousCommits && commits.hasNext()) {
					// Analyze the previous commit 
					RevCommit current = commits.next();
					RevTree tree = current.getTree();
					try {
						TreeWalk walk = new TreeWalk(git.getRepository());
						walk.addTree(tree);
						walk.setRecursive(true);
						currentWalk = walk;
						currentCommitId = current.getId().getName();
					} catch (IOException e) {					
						currentWalk = null;
					}
				} else {
					currentWalk = null;
				}
			} catch (IOException e) {
				currentWalk = null;
			}
		}
		return null;
	}
	
	@Override
	public void close() {
		git.close();
	}
	
	
			
	public class GitObject implements IFile {
		
		private ObjectId obj;
		private String path;
		
		public GitObject(ObjectId obj, String path) {
			this.obj = obj;
			this.path = path;
		}
		
		@Override
		public String getPath() {
			return path;
		}
		
		@Override
		public byte[] read() throws IOException {
			ObjectLoader reader = git.getRepository().newObjectReader().open(obj); 
			if (reader.isLarge()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				reader.copyTo(out);
				return out.toByteArray();
			} else {
				return reader.getCachedBytes();
			}
		}
	}

}
