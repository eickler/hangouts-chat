package c8y.jenkins.hangouts.report.logs;

import hudson.model.Run;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.stream.Collectors;

public class PlainExtractor implements Extractor {
    private final int maxLines;

    public PlainExtractor(int maxLines) {
        this.maxLines = maxLines;
    }

    @Override
    public boolean matches(Run<?, ?> build) {
        return true;
    }

    @Override
    public String extract(Run<?, ?> build) {
        try {
            return build.getLog(maxLines).stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "Not able to extract logs";
        }
    }
}
