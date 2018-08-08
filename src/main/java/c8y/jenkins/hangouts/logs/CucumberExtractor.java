package c8y.jenkins.hangouts.logs;

public class CucumberExtractor implements Extractor {

	public static final String CUCUMBER_MARKER = "Failing Scenarios:";
	public static final String FAILURE_MARKER = "# Scenario: ";

	private int failureCount = 0;
	private StringBuffer failureSummary = new StringBuffer();

	@Override
	public boolean matches(String line) {
		return line.startsWith(CUCUMBER_MARKER);
	}

	@Override
	public void extract(String line, int maxEntries) {
		int index = line.indexOf(FAILURE_MARKER);
		if (index >= 0) {
			failureCount++;
			if (failureCount < maxEntries) {
				String failureText = line.substring(index + FAILURE_MARKER.length()) + "\n";
				failureSummary.append(failureText);
			} else if (failureCount == maxEntries) {
				failureSummary.append("... and unfortunately some more ...\n");
			}
		}
	}

	@Override
	public String getReport() {
		return "There are a total of " + failureCount + " failures.\n" + failureSummary.toString();
	}
}
