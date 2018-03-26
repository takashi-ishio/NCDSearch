package ncdsearch.ncd.folca;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Driver {

	public static void main(String[] args) {
		for (String arg: args) {
			File f = new File(arg);
			FOLCA folca = new FOLCA();
			try {
				long t = System.currentTimeMillis();
				byte[] buf = Files.readAllBytes(f.toPath());
				folca.process(buf);
				folca.finish();
				long time = System.currentTimeMillis() - t;
				assert Arrays.equals(buf, folca.decode());
				System.out.println(arg + ": " + buf.length + " bytes, folca: " + folca.getDictionarySize() + ", " + time + "ms");
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

}
