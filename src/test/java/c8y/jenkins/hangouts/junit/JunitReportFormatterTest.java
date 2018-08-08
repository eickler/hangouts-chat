package c8y.jenkins.hangouts.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import c8y.jenkins.hangouts.junit.JunitReportFormatter;

public class JunitReportFormatterTest {
	public static final String BUILDURL = "http://localhost";
	public static final String REPORTURL = BUILDURL + "/testReport/";
	public static final String TESTNAME = "testcase";
	public static final String SIMPLENAME = JunitReportFormatterTest.class.getSimpleName();
	public static final String CLASSNAME = JunitReportFormatterTest.class.getName();
	public static final String PACKAGENAME = JunitReportFormatterTest.class.getPackage().getName();

	public static final String JENKINSURL = REPORTURL + PACKAGENAME + "/" + SIMPLENAME + "/" + TESTNAME + "/";
	public static final String HANGOUTSLINK = "<" + JENKINSURL + "|c.j.h.j." + SIMPLENAME + "." + TESTNAME + ">";
	
	@Test
	public void appendShortenedClassname() {
		testShortening("Class", "Class");
		testShortening("package.Class", "p.Class");
		testShortening("package.sub.Class", "p.s.Class");
	}

	private void testShortening(String fullName, String expectedShortName) {
		StringBuffer actualShortName = new StringBuffer();
		JunitReportFormatter.appendShortenedClassname(fullName, actualShortName);
		assertEquals(expectedShortName, actualShortName.toString());
	}
	
	@Test
	public void appendJenkinsUrl() {
		StringBuffer actualUrl = new StringBuffer();
		JunitReportFormatter.appendJenkinsUrl(REPORTURL, CLASSNAME, TESTNAME, actualUrl);
		assertEquals(JENKINSURL, actualUrl.toString());
	}
	
	@Test
	public void appendFormattedTestResult() {
		StringBuffer actualUrl = new StringBuffer();
		JunitReportFormatter.appendFormattedTestResult(REPORTURL, CLASSNAME, TESTNAME, actualUrl);
		assertEquals(HANGOUTSLINK, actualUrl.toString());		
	}
}
