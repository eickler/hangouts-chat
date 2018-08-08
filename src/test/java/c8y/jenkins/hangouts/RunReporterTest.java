package c8y.jenkins.hangouts;

import static org.junit.Assert.*;

import org.junit.Test;

public class RunReporterTest {

	@Test
	public void truncateShort() {
		String longString = "a";
		
		String truncatedString = RunReporter.truncate(longString, 1);
		
		assertEquals(longString, truncatedString);
	}

	@Test
	public void truncateShort2() {
		String longString = "a\nb";
		
		String truncatedString = RunReporter.truncate(longString, 2);
		
		assertEquals("a\nb", truncatedString);
	}

	@Test
	public void truncateShort3() {
		String longString = "a\nb\n";
		
		String truncatedString = RunReporter.truncate(longString, 2);
		
		assertEquals("a\nb\n", truncatedString);
	}

	@Test
	public void truncateLong() {
		String longString = "a\nb";
		
		String truncatedString = RunReporter.truncate(longString, 1);
		
		assertEquals("a\n", truncatedString);
	}

}
