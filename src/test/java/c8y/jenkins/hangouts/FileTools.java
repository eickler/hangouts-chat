package c8y.jenkins.hangouts;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Scanner;

public class FileTools {
	public static void compareToReference(String actual, String referenceFile) {
		String reference = getReferenceResult(referenceFile);
		assertEquals("Reference does not match actual parsing result", reference, actual);
	}

	
	@SuppressWarnings("resource")
	public static String getReferenceResult(String inputFile) {
		return new Scanner(getStream(inputFile), "UTF-8").useDelimiter("\\A").next();
	}

	public static InputStream getStream(String file) {
		return FileTools.class.getResourceAsStream(file);
	}
}
