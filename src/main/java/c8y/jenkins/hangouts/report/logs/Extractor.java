package c8y.jenkins.hangouts.report.logs;

import hudson.model.Run;

public interface Extractor {

	boolean matches(Run<?, ?> build);

	String extract(Run<?, ?> build);
}
