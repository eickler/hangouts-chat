package c8y.jenkins.hangouts.logs;

public interface Extractor {
	boolean matches(String line);
	void extract(String line, int maxEntries);
	String getReport();
}
