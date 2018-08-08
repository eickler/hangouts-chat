package c8y.jenkins.hangouts.cucumber;

import static c8y.jenkins.hangouts.cucumber.CucumberConstants.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import c8y.jenkins.hangouts.BuildFileFormatter;
import c8y.jenkins.hangouts.BuildFileParser;

public class CucumberReportParser extends BuildFileParser {

	private String nameMode;

	public CucumberReportParser(String buildPath) {
		super(buildPath + CUCUMBERRESULT);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (FEATURE.equals(qName) || SCENARIO.equals(qName)) {
			nameMode = qName;
		}

		if (NAME.equals(qName)) {
			setCurrentProp(nameMode);
			nameMode = null;
		}

		if (ID.equals(qName) || ERROR_MESSAGE.equals(qName)) {
			setCurrentProp(qName);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		flushCurrentProp();

		if (ERROR_MESSAGE.equals(qName)) {
			flush();
		}
	}

	@Override
	public BuildFileFormatter getFormatter() {
		return new CucumberReportFormatter(getFailures());
	}
}
