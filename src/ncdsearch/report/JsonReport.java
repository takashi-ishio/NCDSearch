package ncdsearch.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import ncdsearch.SearchConfiguration;

public class JsonReport implements IReport {

	private long startTime = System.currentTimeMillis();
	private boolean fragmentsGenerated = false;
	private JsonGenerator gen;
	private SearchConfiguration config;
	
	public JsonReport(SearchConfiguration config, OutputStream w) throws IOException {
	    this.config = config;
		JsonFactory factory = new JsonFactory();
	    gen = factory.createGenerator(w, JsonEncoding.UTF8);
	    gen.useDefaultPrettyPrinter();
	    gen.writeStartObject();
	}
	
	@Override
	public void writeConfig(String attrName, String attrValue) throws IOException {
		gen.writeStringField(attrName, attrValue);
	}
	
	@Override
	public void write(List<Fragment> fragments) throws IOException {
		if (!fragmentsGenerated) {
			gen.writeArrayFieldStart("Result");
			fragmentsGenerated = true;
		}
		for (Fragment fragment: fragments) {
			gen.writeStartObject();
			gen.writeStringField("FileName", fragment.getFilename());
			gen.writeNumberField("StartLine", fragment.getStartLine());
			gen.writeNumberField("EndLine", fragment.getEndLine());
			gen.writeNumberField("Distance", fragment.getDistance());
			if (config.reportPositionDetail()) {
				gen.writeNumberField("StartChar", fragment.getStartCharPositionInLine());
				gen.writeNumberField("EndChar", fragment.getEndCharPositionInLine());
				gen.writeStringField("Tokens", fragment.getTokenString());
			}
			gen.writeEndObject();
		}
	}
	
	@Override
	public void close() throws IOException {
		if (fragmentsGenerated) {
			gen.writeEndArray();
		}
		long time = System.currentTimeMillis() - startTime;
		gen.writeNumberField("Time", time);
		gen.writeEndObject();
		gen.close();
	}
}
