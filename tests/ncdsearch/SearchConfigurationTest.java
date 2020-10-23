package ncdsearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class SearchConfigurationTest {
	
	/**
	 * Translate a single string of parameters into an array of strings and construct a configuration object
	 * @param argline is a line including parameters separated by white spaces
	 * @return a configuration object
	 */
	private SearchConfiguration config(String argline) {
		String[] tokens = argline.split(" ");
		ArrayList<String> validTokens = new ArrayList<>(tokens.length);
		for (String t: tokens) {
			t = t.trim();
			if (!t.isEmpty()) validTokens.add(t);
		}
		return new SearchConfiguration(validTokens.toArray(new String[0]));
	}

	@Test
	public void testConfigurations() {
		Assert.assertFalse(config("").isValidConfiguration());
		Assert.assertTrue(config(". -q query.txt -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos").isValidConfiguration());
	}
	
	@Test
	public void testFileListConfiguration() {
		SearchConfiguration c = config("-l NON-EXISTENT-FILE.txt -e file text"); 
		Assert.assertEquals(0, c.getSourceDirs().size());
		Assert.assertFalse("invalid if the file list does not exist", c.isValidConfiguration());
		
		try {
			File f = File.createTempFile("filelist", ".txt");
			f.deleteOnExit();
			c = config("-l " + f.getAbsolutePath() + " -e file text");
			Assert.assertTrue("Valid because the specified file list exists", c.isValidConfiguration());
		} catch (IOException e) {
			Assert.fail("Failed to create a temporary file for testing");
		}
	}
	
	@Test
	public void testGitConfiguration() {
		// This test assumes the current directory is the project directory managed by git
		Assert.assertTrue(config("-git . -q query.txt -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos").isValidConfiguration());

		SearchConfiguration git = config("-git NON-EXISTING-DIR -q query.txt -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos");
		Assert.assertEquals(0, git.getSourceDirs().size());
		Assert.assertFalse(git.isValidConfiguration());
	}
}
