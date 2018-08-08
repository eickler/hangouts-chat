package c8y.jenkins.hangouts.junit;

import static c8y.jenkins.hangouts.junit.JunitConstants.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import c8y.jenkins.hangouts.BuildFileFormatter;
import c8y.jenkins.hangouts.BuildFileParser;

public class JunitReportParser extends BuildFileParser {

	private boolean isFailed;

	public JunitReportParser(String buildPath) {
		super(buildPath + JUNITRESULT);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (CLASS_NAME.equals(qName) || TEST_NAME.equals(qName) || ERROR_DETAILS.equals(qName)) {
			setCurrentProp(qName);
		} 

		if (ERROR_DETAILS.equals(qName)) {
			isFailed = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		flushCurrentProp();

		if (CASE.equals(qName) && isFailed) {
			flush();
			isFailed = false;
		}
	}

	@Override
	public BuildFileFormatter getFormatter() {
		return new JunitReportFormatter(getFailures());
	}
}
