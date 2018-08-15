package c8y.jenkins.hangouts;

import java.util.List;

/**
 * A BuildFileFormatter produces a short textual report from the failures found
 * in a build file as determined by BuildFileParser.
 * 
 * @author eickler
 *
 */
public abstract class BuildFileFormatter {
	private List<ParsedProps> failures;

	public BuildFileFormatter(List<ParsedProps> failures) {
		this.failures = failures;
	}

	public String getReport(String buildUrl) {
		StringBuffer report = new StringBuffer();

		for (ParsedProps failure : failures) {
			format(buildUrl, failure, report);
			report.append("\n");
		}

		return report.toString();
	}

	protected abstract void format(String buildUrl, ParsedProps failure, StringBuffer report);

}
