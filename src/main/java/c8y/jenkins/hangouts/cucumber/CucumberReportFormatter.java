package c8y.jenkins.hangouts.cucumber;

import static c8y.jenkins.hangouts.cucumber.CucumberConstants.CUCUMBERURL;
import static c8y.jenkins.hangouts.cucumber.CucumberConstants.ERROR_MESSAGE;
import static c8y.jenkins.hangouts.cucumber.CucumberConstants.FEATURE;
import static c8y.jenkins.hangouts.cucumber.CucumberConstants.ID;
import static c8y.jenkins.hangouts.cucumber.CucumberConstants.SCENARIO;

import java.util.List;

import c8y.jenkins.hangouts.BuildFileFormatter;
import c8y.jenkins.hangouts.ParsedProps;

public class CucumberReportFormatter extends BuildFileFormatter {
	
	public CucumberReportFormatter(List<ParsedProps> failures) {
		super(failures);
	}
	
	protected void format(String buildUrl, ParsedProps failure, StringBuffer report) {
		appendScenario(buildUrl + CUCUMBERURL, failure.get(ID), failure.get(FEATURE), failure.get(SCENARIO),
				report);
		report.append("\n  ");
		report.append(failure.get(ERROR_MESSAGE).split("\n")[0]);
	}
	
	static void appendScenario(String buildUrl, String id, String feature, String scenario, StringBuffer buffer) {
		buffer.append("<");
		appendJenkinsUrl(buildUrl, id, buffer);
		buffer.append("|");
		buffer.append(feature);
		buffer.append(": ");
		buffer.append(scenario);
		buffer.append(">");
	}

	static void appendJenkinsUrl(String buildUrl, String id, StringBuffer buffer) {
		buffer.append(buildUrl);
		buffer.append(id.replace(';', '/'));
	}
}
