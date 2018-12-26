package ncdsearch.eval;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import sarf.lexer.DirectoryScan;
import sarf.lexer.FileType;
import sarf.lexer.TokenReader;
import sarf.lexer.TokenReaderFactory;

/**
 * Source file 
 */
public class SourceFileList {

	private static final String ARG_LANGUAGE = "-lang";
	
	private int fileCount;
	private long totalLines;
	private int errorFileCount;
	private long totalBytes;
	
	public static void main(String[] args) {
		ArrayList<String> locations = new ArrayList<>(); 
		FileType langFilter = null;

		int idx = 0;
		while (idx < args.length) {
			if (args[idx].equals(ARG_LANGUAGE)) {
				idx++;
				if (idx < args.length) {
					FileType t = TokenReaderFactory.getFileType("." + args[idx++]);
					if (TokenReaderFactory.isSupported(t)) {
						langFilter = t;
					}
				}
			} else {
				locations.add(args[idx++]);
			}
		}		
		
		for (String loc: locations) {
			SourceFileList counter = new SourceFileList();
			counter.process(loc, langFilter);
			System.out.println(loc + "," + counter.toString());
		}
	}
	
	public SourceFileList() {
	}
	
	public String toString() {
		return fileCount + "," + errorFileCount + "," + totalLines + "," + totalBytes;
	}
	
	public void process(String location, FileType langFilter) {
		DirectoryScan.scan(new File(location), new DirectoryScan.Action() {
			
			@Override
			public void process(File f) {
				FileType t = TokenReaderFactory.getFileType(f.getAbsolutePath());
				if (TokenReaderFactory.isSupported(t) && 
					(langFilter == null || langFilter == t)) {
					try {
						byte[] buf = Files.readAllBytes(f.toPath());
						TokenReader r = TokenReaderFactory.create(t, buf, StandardCharsets.UTF_8);
						if (r != null) {
							int line = 0;
							int lastLine = -1;
							while (r.next()) {
								int l = r.getLine();
								if (l != lastLine) {
									lastLine = l;
									line++;
								}
							}
							
							fileCount++;
							totalLines += line;
							totalBytes += buf.length;
						}
					} catch (IOException e) {
						errorFileCount++;
					}
				}
			}
		});
	}

}
