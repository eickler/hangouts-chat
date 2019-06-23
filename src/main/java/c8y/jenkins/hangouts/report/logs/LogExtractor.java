package c8y.jenkins.hangouts.report.logs;

import hudson.model.Run;

public class LogExtractor {
    private final Run<?, ?> build;
    private final Extractor[] extractors;

    public LogExtractor(Run<?, ?> build, int maxEntries) {
        this.build = build;
        this.extractors = new Extractor[]{new MavenExtractor(maxEntries), new PlainExtractor(maxEntries)};
    }

    public String extract() {
        for (Extractor extractor : extractors) {
            if (extractor.matches(build)) {
                return extractor.extract(build);
            }
        }

        return "Not able to extract files";
    }

}
