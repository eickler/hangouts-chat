package c8y.jenkins.hangouts.junit;

import org.junit.Test;

import c8y.jenkins.hangouts.FileTools;
import c8y.jenkins.hangouts.junit.JunitReportParser;

public class JunitReportParserTest {
	public static final String BUILDURL = "http://localhost";
	
	@Test
	public void failureReport() {
		JunitReportParser parser = new JunitReportParser("");
		parser.tryParse(FileTools.getStream("junitResult.xml"));
		String report = parser.getFormatter().getReport(BUILDURL);
		FileTools.compareToReference(report, "junitReference.txt");
	}
}
