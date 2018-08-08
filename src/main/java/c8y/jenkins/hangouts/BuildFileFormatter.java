package c8y.jenkins.hangouts;

import java.util.List;

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
