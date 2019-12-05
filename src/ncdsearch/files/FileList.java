package ncdsearch.files;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class FileList implements IFiles {

	private LineNumberReader reader;
	
	public FileList(File list) {
		try {
			reader = new LineNumberReader(new FileReader(list));
		} catch (IOException e) {
		}
	}
	
	@Override
	public File next() {
		if (reader != null) {
			try {
				String line = reader.readLine();
				if (line != null) {
					return new File(line);
				}
			} catch (IOException e) {
			}
			// End of File or IOException
			try {
				reader.close();
			} catch (IOException e) {
			}
			reader = null;
		}
		return null;
	}
	
	@Override
	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
			}
			reader = null;
		}		
	}
}
