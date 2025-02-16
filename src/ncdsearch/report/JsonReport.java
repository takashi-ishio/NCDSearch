package ncdsearch.report;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import ncdsearch.SearchConfiguration;

public class JsonReport extends AbstractReport {

	private boolean fragmentsGenerated = false;
	private JsonGenerator gen;
	private SearchConfiguration config;
	
	public JsonReport(SearchConfiguration config, OutputStream w) throws IOException {
		super(config);
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
	protected void writeFragment(Fragment fragment) throws IOException {
		writeStartOfFragmentsIfNecessary();
		
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

	private void writeStartOfFragmentsIfNecessary() throws IOException {
		if (!fragmentsGenerated) {
			gen.writeArrayFieldStart("Result");
			fragmentsGenerated = true;
		}
	}

	private void writeEndOfFragmentsIfNecessary() throws IOException {
		if (fragmentsGenerated) {
			gen.writeEndArray();
			fragmentsGenerated = false;
		}
	}
	
	@Override
	public void writeNumberField(String name, long value) throws IOException {
		writeEndOfFragmentsIfNecessary();
		gen.writeNumberField(name, value);
	}
	
	@Override
	public void doClose() throws IOException {
		writeEndOfFragmentsIfNecessary();
		gen.writeEndObject();
		gen.close();
	}
}
