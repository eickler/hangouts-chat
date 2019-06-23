package c8y.jenkins.hangouts;

import com.google.common.base.Charsets;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;

public class FileTools {

    @SuppressWarnings("resource")
    public static String load(String inputFile) {
        return new Scanner(openStream(inputFile), "UTF-8").useDelimiter("\\A").next();
    }

    public static InputStream openStream(String file) {
        return FileTools.class.getResourceAsStream(file);
    }

    public static Reader openReader(String file) {
        return new InputStreamReader(FileTools.class.getResourceAsStream(file), Charsets.UTF_8);
    }


}
