package ncdsearch.report;

import java.io.IOException;

import ncdsearch.SearchConfiguration;

public class StdoutReport extends AbstractReport {
	
	private SearchConfiguration config;

	
	public StdoutReport(SearchConfiguration config) {
		super(config);
		this.config = config;
	}
	
	public void writeConfig(String attrName, String attrValue) {
		if (config.isVerbose()) {
			System.err.println(attrName + ": " + attrValue);
		}
	}

	@Override
	protected void writeFragment(Fragment fragment) throws IOException {
		System.out.write(fragment.toString(config.reportPositionDetail(), config.getLinkStyle()).getBytes());
	}
	
	@Override
	public void writeNumberField(String name, long value) {
		System.err.println(name + ": " + value);
	}
	
	@Override
	public void doClose() throws IOException {
		// nothing to do
	}
	
}
