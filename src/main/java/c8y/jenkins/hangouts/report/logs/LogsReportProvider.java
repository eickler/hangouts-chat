package c8y.jenkins.hangouts.report.logs;

import c8y.jenkins.hangouts.ReportProvider;
import hudson.model.Run;

import java.io.IOException;
import java.io.Reader;

public class LogsReportProvider implements ReportProvider {
    private final Run<?, ?> build;
    private final int maxLogEntries;

    public LogsReportProvider(Run<?, ?> build, int maxLogEntries) {
        this.build = build;
        this.maxLogEntries = maxLogEntries;
    }

    @Override
    public boolean canProvide() {
        try (Reader log = build.getLogReader()) {
            return true;
        } catch (IOException ex) {
            return false;
        }

    }

    @Override
    public String report() {
        LogExtractor extractor = new LogExtractor(build, maxLogEntries);
        return extractor.extract();
    }
}
