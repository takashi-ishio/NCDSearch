package ncdsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
	public void testDefaultConfiguration() {
		Assert.assertFalse(config("").isValidConfiguration());
	}

	@Test
	public void testQueryFileConfiguration() {
		try {
			File f = createTempQueryFile("int x");
			Assert.assertTrue(Files.readAllLines(f.toPath()).get(0).equals("int x"));
			Assert.assertTrue(config(". -q " + f.getAbsolutePath() + " -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos").isValidConfiguration());
		} catch (IOException e) {
			Assert.fail("Failed to create a temporary file for testing");
		}
	}
	
	private File createTempQueryFile(String content) throws IOException {
		File f = File.createTempFile("query", ".txt");
		Files.write(f.toPath(), content.getBytes());
		f.deleteOnExit();
		return f;
	}

	@Test
	public void testEmptyFileConfiguration() {
		Assert.assertFalse(config("").isValidConfiguration());
		try {
			File f = createTempQueryFile(" ");
			Assert.assertFalse("Empty file is an invalid configuration", config(". -q " + f.getAbsolutePath() + " -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos").isValidConfiguration());
		} catch (IOException e) {
			Assert.fail("Failed to create a temporary file for testing");
		}
	}

	@Test
	public void testFileListConfiguration() {
		SearchConfiguration c = config("-l NON-EXISTENT-FILE.txt -e file text"); 
		Assert.assertEquals(0, c.getSourceDirs().size());
		Assert.assertFalse("invalid if the file list does not exist", c.isValidConfiguration());
		
		try {
			File f = createTempQueryFile(" ");
			c = config("-l " + f.getAbsolutePath() + " -e file text");
			Assert.assertTrue("Valid because the specified file list exists", c.isValidConfiguration());
		} catch (IOException e) {
			Assert.fail("Failed to create a temporary file for testing");
		}
	}
	
	@Test
	public void testGitConfiguration() {
		// This test assumes the current directory is the project directory managed by git
		try {
			File f = createTempQueryFile("int x");
			Assert.assertTrue(config("-git . -q " + f.getAbsolutePath() + " -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos").isValidConfiguration());
	
			SearchConfiguration git = config("-git NON-EXISTING-DIR -q query.txt -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos");
			Assert.assertEquals(0, git.getSourceDirs().size());
			Assert.assertFalse(git.isValidConfiguration());
		} catch (IOException e) {
			Assert.fail("Failed to create a temporary file for testing");
		}
	}
}
