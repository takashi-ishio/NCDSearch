package ncdsearch;

import org.junit.Assert;
import org.junit.Test;

public class SearchConfigurationTest {
	
	private SearchConfiguration config(String s) {
		return new SearchConfiguration(s.split(" "));
	}

	@Test
	public void testConfigurations() {
		Assert.assertFalse(config("").isValidConfiguration());
		Assert.assertTrue(config(". -q query.txt -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos").isValidConfiguration());
		Assert.assertTrue(config("-git . -q query.txt -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos").isValidConfiguration());
		Assert.assertTrue(config("-l list.txt -q query.txt -lang java -sline 0 -eline 6  -a zip -th 0.6 -json -pos").isValidConfiguration());
	}
}
