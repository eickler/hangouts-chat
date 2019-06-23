package c8y.jenkins.hangouts.report.logs;

import hudson.model.Run;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

public class MavenExtractor implements Extractor {

    public static final String MAVEN_MARKER = "[ERROR] ";
    private final int maxEntries;

    public MavenExtractor(int maxEntries) {
        this.maxEntries = maxEntries;
    }


    private boolean matches(String line) {
        return line.contains(MAVEN_MARKER);
    }


    @Override
    public boolean matches(Run<?, ?> build) {
        try (BufferedReader log = new BufferedReader(build.getLogReader())) {
            return log.lines().anyMatch(this::matches);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String extract(Run<?, ?> build) {
        try (BufferedReader log = new BufferedReader(build.getLogReader())) {
            return log.lines()
                    .map(l->l.replaceAll("^[\\u001B]\\[.*[\\u001B].*?\\[\\d+m",""))
                    .filter(dropWhileNotMatch()).limit(maxEntries)
                    .collect(joining("\n"));
        } catch (IOException e) {
            return "Not able to extract logs";
        }
    }

    private Predicate<? super String> dropWhileNotMatch() {
        return new Predicate<String>() {
            private boolean matched = false;

            @Override
            public boolean test(String line) {
                return matched || (matched = matches(line));
            }
        };
    }
}
