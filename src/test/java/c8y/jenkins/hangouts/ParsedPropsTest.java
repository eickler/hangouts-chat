package c8y.jenkins.hangouts;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParsedPropsTest {
	public static final String TESTPROP = "testprop";
	public static final String TESTVALUE = "testvalue";
	public static final String TESTPROP2 = "testprop2";
	public static final String TESTVALUE2 = "testvalue2";
	
	private ParsedProps props = new ParsedProps();

	@Test
	public void addProp() {
		props.setCurrentProp(TESTPROP);
		
		props.characters(TESTVALUE.toCharArray(), 0, TESTVALUE.length());
		props.flushCurrentProp();
		
		assertEquals(TESTVALUE, props.get(TESTPROP));
	}

	@Test
	public void ignoreProp() {
		props.characters(TESTVALUE.toCharArray(), 0, TESTVALUE.length());
		props.flushCurrentProp();

		assertEquals(0, props.props.size());
	}

	@Test
	public void resetProp() {
		props.setCurrentProp(TESTPROP);

		props.characters(TESTVALUE.toCharArray(), 0, TESTVALUE.length());
		props.flushCurrentProp();
		props.characters(TESTVALUE2.toCharArray(), 0, TESTVALUE2.length());
		props.flushCurrentProp();

		assertEquals(TESTVALUE.toString(), props.get(TESTPROP));
	}

	@Test
	public void addProps() {
		props.setCurrentProp(TESTPROP);
		props.characters(TESTVALUE.toCharArray(), 0, TESTVALUE.length());
		props.flushCurrentProp();


		props.setCurrentProp(TESTPROP2);
		props.characters(TESTVALUE2.toCharArray(), 0, TESTVALUE2.length());
		props.flushCurrentProp();
		
		assertEquals(TESTVALUE.toString(), props.get(TESTPROP));	
		assertEquals(TESTVALUE2.toString(), props.get(TESTPROP2));	
	}

}
