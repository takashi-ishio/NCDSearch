package ncdsearch.files;

import java.io.IOException;

/**
 * This class represents a source file to be analyzed.
 */
public interface IFile {

    /**
     * Returns the path of the file as a string for output.
     * @return the file path
     */
	public String getPath();

    /**
     * Reads the entire content of the file and returns it as a byte array.
     *
     * @return a byte array containing the contents of the file
     * @throws IOException if an I/O error occurs while reading the file
     */
	public byte[] read() throws IOException;
}
