package ncdsearch.report;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import ncdsearch.Fragment;
import ncdsearch.SearchConfiguration;

public class JsonReport implements IReport {

	private long startTime = System.currentTimeMillis();
	private boolean firstFragment = true;
	private JsonGenerator gen;
	private SearchConfiguration config;
	
	public JsonReport(SearchConfiguration config, OutputStream w) throws IOException {
	    this.config = config;
		JsonFactory factory = new JsonFactory();
	    JsonGenerator gen = factory.createGenerator(w, JsonEncoding.UTF8);
	    gen.useDefaultPrettyPrinter();
	    gen.writeStartObject();
	}
	
	@Override
	public void writeConfig(String attrName, String attrValue) throws IOException {
		gen.writeStringField(attrName, attrValue);
	}
	
	@Override
	public void write(List<Fragment> fragments) throws IOException {
		if (firstFragment) {
			gen.writeArrayFieldStart("fragments"); 
		}
		for (Fragment fragment: fragments) {
			gen.writeStartObject();
			gen.writeStringField("filename", fragment.getFilename());
			gen.writeNumberField("startline", fragment.getStartLine());
			gen.writeNumberField("endline", fragment.getEndLine());
			gen.writeNumberField("distance", fragment.getDistance());
			if (config.reportPositionDetail()) {
				gen.writeNumberField("startchar", fragment.getStartCharPositionInLine());
				gen.writeNumberField("endchar", fragment.getEndCharPositionInLine());
				gen.writeStringField("tokens", fragment.getTokenString());
			}
			gen.writeEndObject();
		}
	}
	
	@Override
	public void close() throws IOException {
		gen.writeEndArray();
		long time = System.currentTimeMillis() - startTime;
		gen.writeNumberField("time-ms", time);
		gen.writeEndObject();
		gen.close();
	}
}
