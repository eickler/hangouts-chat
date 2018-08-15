package c8y.jenkins.hangouts;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to extract sets of element content (&lt;element&gt;content&lt;/element&gt;) from an XML file using SAX. 
 * @author eickler
 *
 */
public class ParsedProps {
	Map<String,String> props;
	String currentProp;
	StringBuffer currentValue;

	public ParsedProps() {
		this.props = new HashMap<String,String>();
	}
	
	public ParsedProps(ParsedProps otherProps) {
		this.props = new HashMap<String,String>(otherProps.props);
	}

	public void startParsingOf(String prop) {
		endParsingAndStoreContent();
		this.currentProp = prop;
		this.currentValue = new StringBuffer();
	}
	
	public void endParsingAndStoreContent() {
		if (currentProp != null) {
			props.put(currentProp, currentValue.toString());
			this.currentProp = null;
			this.currentValue = null;
		}
	}
	
	public String get(String prop) {
		return props.get(prop);
	}
	
	/**
	 * Gather element content from potentially multiple chunks passed in by SAX.
	 * @param ch Chunk of characters
	 * @param start Start character
	 * @param length Number of characters
	 */
	public void characters(char[] ch, int start, int length) {
		if (currentProp != null) {
			currentValue.append(ch, start, length);
		}
	}
}
