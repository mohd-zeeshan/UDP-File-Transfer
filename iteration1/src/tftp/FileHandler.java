package tftp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


/**
 * A helper class which has some class method related to file i/o
 *
 */
public class FileHandler {

	/**
	 * Writes content to the file named filename
	 * @param filename
	 * @param content
	 */
	public static void writeToFile(String filename, byte[] content) {
		File file = new File(filename);;
		FileOutputStream fop = null;
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			fop = new FileOutputStream(file, true);
			fop.write(content);
			fop.flush();
			fop.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Removes the trailing null bytes (zero)
	 * @param bytes
	 * @return
	 */
	public static byte[] trim(byte[] bytes) {
	    int i = bytes.length - 1;
	    while (i >= 0 && bytes[i] == 0) {
	        --i;
	    }
	    return Arrays.copyOf(bytes, i + 1);
	}

}
