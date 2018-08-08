package c8y.jenkins.hangouts;

import java.util.HashMap;
import java.util.Map;

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
	
	public void setCurrentProp(String prop) {
		flushCurrentProp();
		this.currentProp = prop;
		this.currentValue = new StringBuffer();
	}
	
	public void flushCurrentProp() {
		if (currentProp != null) {
			props.put(currentProp, currentValue.toString());
			this.currentProp = null;
			this.currentValue = null;
		}
	}
	
	public String get(String prop) {
		return props.get(prop);
	}
	
	public void characters(char[] ch, int start, int length) {
		if (currentProp != null) {
			currentValue.append(ch, start, length);
		}
	}
}
