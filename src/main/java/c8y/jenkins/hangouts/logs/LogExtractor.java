package c8y.jenkins.hangouts.logs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LogExtractor {
	private BufferedReader log;
	private int maxEntries;
	private Extractor[] extractors = { new MavenExtractor(), new CucumberExtractor() };

	public LogExtractor(Reader log, int maxEntries) {
		this.log = new BufferedReader(log);
		this.maxEntries = maxEntries;
	}

	public String extract() throws IOException {
		String result = "";
		
		Extractor extractor = findExtractor();
		if (extractor != null) {
			return getSummary(extractor);
		}
		
		return result;
	}

	private Extractor findExtractor() throws IOException {
		String line; 
		while ((line = log.readLine()) != null) {
			for (Extractor extractor : extractors ) {
				if (extractor.matches(line)) {
					return extractor;
				}
			}
		}
		return null;
	}

	private String getSummary(Extractor extractor) throws IOException {
		String line; 
		while ((line = log.readLine()) != null) {
			extractor.extract(line, maxEntries);
		}
		return extractor.getReport();
	}
}
