package c8y.jenkins.hangouts.cucumber;

import org.junit.Test;

import c8y.jenkins.hangouts.FileTools;
import c8y.jenkins.hangouts.cucumber.CucumberReportParser;

public class CucumberReportParserTest {	
	public static final String BUILDURL = "http://localhost";

	@Test
	public void testReference() {
		CucumberReportParser parser = new CucumberReportParser("");
		parser.tryParse(FileTools.getStream("cucumberResult.xml"));
		String report = parser.getFormatter().getReport(BUILDURL);
		FileTools.compareToReference(report, "cucumberReference.txt");
	}	
}
