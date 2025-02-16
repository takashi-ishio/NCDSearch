package ncdsearch.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ncdsearch.SearchConfiguration;
import ncdsearch.comparison.TokenSequence;

public class JsonReportTest {

	@Test
	public void testTime() throws IOException {
		SearchConfiguration config = new SearchConfiguration(new String[] {"-json", "-time"});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonReport r = new JsonReport(config, out);
		r.recordAnalyzedFile("", 100, 200);
		ArrayList<Fragment> data = new ArrayList<Fragment>();
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.1));
		r.write(data);
		r.close();
		
		// Check the correctness of JSON format
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(out.toByteArray());
		JsonNode time = node.findValue("Files");
		Assert.assertEquals(1, time.asLong());
		JsonNode tokens = node.findValue("Tokens");
		Assert.assertEquals(200, tokens.asLong());
	}
	
	@Test
	public void testSort() throws IOException {
		SearchConfiguration config = new SearchConfiguration(new String[] {"-json", "-sort"});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonReport r = new JsonReport(config, out);
		ArrayList<Fragment> data = new ArrayList<Fragment>();
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.2));
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.1));
		r.write(data);
		r.close();
		
		// Check the correctness of JSON format
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(out.toByteArray());
		JsonNode results = node.findValue("Result");
		Assert.assertTrue(results.isArray());
		JsonNode fragment1 = results.get(0); 
		Assert.assertEquals(0.1, fragment1.get("Distance").asDouble(), 0.01);
		JsonNode fragment2 = results.get(1); 
		Assert.assertEquals(0.2, fragment2.get("Distance").asDouble(), 0.01);
	}

	@Test
	public void testSortedTime() throws IOException {
		SearchConfiguration config = new SearchConfiguration(new String[] {"-json", "-sort", "-time"});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonReport r = new JsonReport(config, out);
		r.recordAnalyzedFile("", 100, 200);
		r.recordAnalyzedFile("", 200, 300);
		ArrayList<Fragment> data = new ArrayList<Fragment>();
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.2));
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.1));
		r.write(data);
		r.close();
		
		// Check the correctness of JSON format
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(out.toByteArray());
		JsonNode results = node.findValue("Result");
		Assert.assertTrue(results.isArray());
		JsonNode fragment1 = results.get(0); 
		Assert.assertEquals(0.1, fragment1.get("Distance").asDouble(), 0.01);
		JsonNode fragment2 = results.get(1); 
		Assert.assertEquals(0.2, fragment2.get("Distance").asDouble(), 0.01);

		JsonNode time = node.findValue("Files");
		Assert.assertEquals(2, time.asLong());
		JsonNode lines = node.findValue("Lines");
		Assert.assertEquals(300, lines.asLong());
		JsonNode tokens = node.findValue("Tokens");
		Assert.assertEquals(500, tokens.asLong());
	}

	@Test
	public void testUnsorted() throws IOException {
		SearchConfiguration config = new SearchConfiguration(new String[] {"-json"});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonReport r = new JsonReport(config, out);
		ArrayList<Fragment> data = new ArrayList<Fragment>();
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.2));
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.1));
		r.write(data);
		r.close();
		
		// Check the correctness of JSON format
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(out.toByteArray());
		JsonNode results = node.findValue("Result");
		Assert.assertTrue(results.isArray());
		JsonNode fragment1 = results.get(0); 
		Assert.assertEquals(0.2, fragment1.get("Distance").asDouble(), 0.01);
		JsonNode fragment2 = results.get(1); 
		Assert.assertEquals(0.1, fragment2.get("Distance").asDouble(), 0.01);
	}

	@Test
	public void testDetailed() throws IOException {
		SearchConfiguration config = new SearchConfiguration(new String[] {"-json", "-pos"});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonReport r = new JsonReport(config, out);
		ArrayList<Fragment> data = new ArrayList<Fragment>();
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.2));
		data.add(new Fragment("test.txt", new TokenSequence("TEST"), 0, 1, 0.1));
		r.write(data);
		r.close();
		
		// Check the correctness of JSON format
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(out.toByteArray());
		JsonNode results = node.findValue("Result");
		Assert.assertTrue(results.isArray());
		JsonNode fragment1 = results.get(0); 
		Assert.assertEquals(0, fragment1.get("StartChar").asInt());
	}

}
