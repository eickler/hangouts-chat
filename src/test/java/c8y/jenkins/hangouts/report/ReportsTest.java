package c8y.jenkins.hangouts.report;

import org.junit.Test;

import static c8y.jenkins.hangouts.report.Reports.dropCharacters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ReportsTest {

	@Test
	public void truncateShort() {
		String longString = "a";
		
		String truncatedString = Reports.truncate(longString, 1);
		
		assertEquals(longString, truncatedString);
	}

	@Test
	public void truncateShort2() {
		String longString = "a\nb";
		
		String truncatedString = Reports.truncate(longString, 2);
		
		assertEquals("a\nb", truncatedString);
	}

	@Test
	public void truncateShort3() {
		String longString = "a\nb\n";
		
		String truncatedString = Reports.truncate(longString, 2);
		
		assertEquals("a\nb\n", truncatedString);
	}

	@Test
	public void truncateLong() {
		String longString = "a\nb";
		
		String truncatedString = Reports.truncate(longString, 1);
		
		assertEquals("a\n", truncatedString);
	}

	@Test
	public void shouldDropUnwantedCharacter() {
		StringBuilder text = new StringBuilder("a \"b\" c");

		dropCharacters(text, '"');

		assertThat(text.toString()).isEqualTo("a b c");
	}

}
