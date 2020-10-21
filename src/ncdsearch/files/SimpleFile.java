package ncdsearch.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SimpleFile implements IFile {

	private File file;
	
	public SimpleFile(File f) {
		this.file = f;
	}
	
	@Override
	public String getPath() {
		return file.getAbsolutePath();
	}
	
	@Override
	public byte[] read() throws IOException {
		return Files.readAllBytes(file.toPath());
	}
}
