package c8y.jenkins.hangouts;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A BuildFileParser parses XML test reports and produces a list of failures
 * that can be passed to a BuildFileFormatter. Each failure consists of various
 * properties describing the failure.
 * 
 * @author eickler
 *
 */
public abstract class BuildFileParser extends DefaultHandler {

	private String filename;

	private List<ParsedProps> failures;
	private ParsedProps currentFailure;

	public BuildFileParser(String filename) {
		this.filename = filename;
	}

	public boolean tryParse() {
		try (InputStream is = new FileInputStream(filename)) {
			return tryParse(is);
		} catch (IOException e) {
			return false;
		}
	}

	public boolean tryParse(InputStream is) {
		try {
			failures = new ArrayList<ParsedProps>();
			currentFailure = new ParsedProps();
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			saxParser.parse(is, this);
			return true;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return false;
		}
	}

	public void startParsingOf(String prop) {
		currentFailure.startParsingOf(prop);
	}

	public void endParsingAndStoreContent() {
		currentFailure.endParsingAndStoreContent();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		currentFailure.characters(ch, start, length);
	}

	public void flush() {
		failures.add(new ParsedProps(currentFailure));
	}

	public List<ParsedProps> getFailures() {
		return failures;
	}

	public abstract BuildFileFormatter getFormatter();
}
