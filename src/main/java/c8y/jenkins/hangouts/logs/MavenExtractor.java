package c8y.jenkins.hangouts.logs;

public class MavenExtractor implements Extractor {

	public static final String MAVEN_MARKER = "[ERROR] ";

	private int lineCount = 0;
	private StringBuffer failureSummary = new StringBuffer();

	@Override
	public boolean matches(String line) {
		return matchAndAdd(line);
	}

	@Override
	public void extract(String line, int maxEntries) {
		matchAndAdd(line);
	}

	private boolean matchAndAdd(String line) {
		boolean match = line.startsWith(MAVEN_MARKER); 

		if (match && lineCount < 2 && line.length() > MAVEN_MARKER.length()) {
			failureSummary.append(line.substring(MAVEN_MARKER.length()));
			lineCount++;
		}

		return match;
	}

	@Override
	public String getReport() {
		return failureSummary.toString();
	}
}
