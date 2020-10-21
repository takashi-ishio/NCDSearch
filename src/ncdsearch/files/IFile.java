package ncdsearch.files;

import java.io.IOException;

public interface IFile {

	public String getPath();
	
	/**
	 * Read all bytes in the file entity.
	 * @return
	 * @throws IOException
	 */
	public byte[] read() throws IOException;
}
