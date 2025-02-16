package ncdsearch.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ncdsearch.SearchConfiguration;

public abstract class AbstractReport implements IReport {

	private int fileCount = 0;
	private long lineCount = 0;
	private long tokenCount = 0;
	
	private List<Fragment> results = new ArrayList<Fragment>();
	private long startTime = System.currentTimeMillis();
	private SearchConfiguration config;
	
	protected AbstractReport(SearchConfiguration config) {
		this.config = config;
	}
	
	@Override
	public synchronized void write(List<Fragment> fragments) throws IOException {
		if (config.sortResult()) {
			results.addAll(fragments);
		} else {
			// Simply write the result
			for (Fragment fragment: fragments) {
				writeFragment(fragment);
			}
		}
	}
	
	protected abstract void writeFragment(Fragment fragment) throws IOException;

	/**
	 * This method records files processed by the program.
	 * This method has "synchronized" because multiple threads may call this method.
	 */
	@Override 
	public synchronized void recordAnalyzedFile(String filename, int lines, int tokens) {
		fileCount++;
		lineCount += lines;
		tokenCount += tokens;
	}

	@Override
	public void close() throws IOException {
		if (config.sortResult()) {
			// Sort and output the result
			results.sort(new Comparator<Fragment>() {
				public int compare(Fragment f1, Fragment f2) {
					int comparison = Double.compare(f1.getDistance(), f2.getDistance());
					if (comparison == 0) {
						comparison = f1.compareTo(f2);
					}
					return comparison;
				}
			});
			try {
				for (Fragment fragment: results) {
					writeFragment(fragment);
				}
			} catch (IOException e) {
			}
		}
		
		if (config.reportTime()) {	
			long time = System.currentTimeMillis() - startTime;
			writeNumberField("Time", time);
			writeNumberField("Files", fileCount);
			writeNumberField("Lines", lineCount);
			writeNumberField("Tokens", tokenCount);
		}
		doClose();
	}
	
	/**
	 * Write a number field after the report
	 * @param name specifies an attribute name.
	 * @param value specifies a value.
	 */
	public abstract void writeNumberField(String name, long value) throws IOException;
	
	/**
	 * This method implements a procedure of closing.
	 */
	public abstract void doClose() throws IOException;

}
