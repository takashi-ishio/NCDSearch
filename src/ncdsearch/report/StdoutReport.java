package ncdsearch.report;

import java.io.IOException;
import java.util.List;

import ncdsearch.Fragment;
import ncdsearch.SearchConfiguration;

public class StdoutReport implements IReport {
	
	private long startTime = System.currentTimeMillis();
	private SearchConfiguration config;

	public StdoutReport(SearchConfiguration config) {
		this.config = config;
	}
	
	public void writeConfig(String attrName, String attrValue) {
		if (config.isVerbose()) {
			System.err.println(attrName + ": " + attrValue);
		}
	}

	@Override
	public synchronized void write(List<Fragment> fragments) throws IOException {
		for (Fragment fragment: fragments) {
			if (config.reportPositionDetail()) {
				System.out.write(fragment.toLongString().getBytes());
			} else {
				System.out.write(fragment.toString().getBytes());
			}
		}
	}
	
	public void close() {
		if (config.reportTime()) {
			long time = System.currentTimeMillis() - startTime;
			System.err.println("Time (ms): " + time);
		}
		
	}
}
