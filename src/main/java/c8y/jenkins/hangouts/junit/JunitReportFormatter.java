package c8y.jenkins.hangouts.junit;

import static c8y.jenkins.hangouts.junit.JunitConstants.CLASS_NAME;
import static c8y.jenkins.hangouts.junit.JunitConstants.ERROR_DETAILS;
import static c8y.jenkins.hangouts.junit.JunitConstants.JUNITURL;
import static c8y.jenkins.hangouts.junit.JunitConstants.TEST_NAME;

import java.util.List;

import c8y.jenkins.hangouts.BuildFileFormatter;
import c8y.jenkins.hangouts.ParsedProps;

public class JunitReportFormatter extends BuildFileFormatter {
	
	public JunitReportFormatter(List<ParsedProps> failures) {
		super(failures);
	}

	public void format(String buildUrl, ParsedProps failure, StringBuffer report) {
		appendFormattedTestResult(buildUrl+ JUNITURL, failure.get(CLASS_NAME), failure.get(TEST_NAME), report);
		report.append(": ");
		report.append(failure.get(ERROR_DETAILS));
	}

	static void appendFormattedTestResult(String baseUrl, String className, String testName, StringBuffer buffer) {
		buffer.append("<");
		appendJenkinsUrl(baseUrl, className, testName, buffer);
		buffer.append("|");
		appendShortenedClassname(className, buffer);
		buffer.append(".");
		buffer.append(testName);
		buffer.append(">");
	}

	static void appendJenkinsUrl(String buildUrl, String qualifiedClassName, String testName, StringBuffer buffer) {
		buffer.append(buildUrl);

		int classNameIndex = qualifiedClassName.lastIndexOf('.') + 1;
		if (classNameIndex > 0) {
			buffer.append(qualifiedClassName.substring(0, classNameIndex - 1));
			buffer.append("/");
		}

		buffer.append(qualifiedClassName.substring(classNameIndex));
		buffer.append("/");

		buffer.append(testName);
		buffer.append("/");
	}

	static void appendShortenedClassname(String className, StringBuffer buffer) {
		String[] packages = className.split("[.]");
		int last = packages.length - 1;

		for (int i = 0; i < last; i++) {
			buffer.append(packages[i].charAt(0));
			buffer.append('.');
		}
		buffer.append(packages[last]);
	}
}
