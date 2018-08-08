package c8y.jenkins.hangouts;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import c8y.jenkins.hangouts.cucumber.CucumberReportParser;
import c8y.jenkins.hangouts.junit.JunitReportParser;
import c8y.jenkins.hangouts.logs.LogExtractor;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.User;
import jenkins.model.Jenkins;

public class RunReporter {
	public static final int MAX_LOG_ENTRIES = 10;

	private Run<?, ?> run;

	public RunReporter(Run<?, ?> run) {
		this.run = run;
	}

	public String report(Set<com.google.api.services.chat.v1.model.User> members) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("I am afraid the build of ");
		buffer.append(getJob());
		buffer.append(" broke.\n");

		Set<String> culprits = getCulprits(members);
		if (culprits.size() > 0) {
			buffer.append("Would these ladies and gentlemen please check it:\n");

			for (String culprit : culprits) {
				buffer.append(culprit);
				buffer.append(" ");
			}
			buffer.append("\n");
		}

		// We need to probably do a better job in telling the people how many items there are and if the log was truncated.
		String logSummary = truncate(getLogSummary(), MAX_LOG_ENTRIES);
		buffer.append(logSummary);

		return buffer.toString().replaceAll("\"", "");
	}

	private String getJob() {
		return run.getParent().getDisplayName();
	}

	private Set<String> getCulprits(Set<com.google.api.services.chat.v1.model.User> members) {
		Set<String> result = getCulprits();

		for (com.google.api.services.chat.v1.model.User user : members) {
			if (result.contains(user.getDisplayName())) {
				result.remove(user.getDisplayName());
				result.add("<" + user.getName() + ">");
			}
		}
		
		return result;
	}

	private Set<String> getCulprits() {
		Set<String> result = new HashSet<String>();
		Set<User> users;

		if (run instanceof WorkflowRun) {
			// From Jenkins Pipeline
			users = ((WorkflowRun) run).getCulprits();
		} else if (run instanceof AbstractBuild<?, ?>) {
			// From normal Jobs
			users = ((AbstractBuild<?, ?>) run).getCulprits();
		} else {
			users = new HashSet<User>();
		}

		for (User u : users) {
			String name = u.getFullName();
			name = removeEmailIfPresent(name);
			result.add(name);
		}

		return result;
	}

	private String removeEmailIfPresent(String name) {
		if (name.indexOf('<') > 0) {
			name = name.substring(0, name.indexOf('<') - 1);
		}
		return name;
	}

	private String getLogSummary() {
		String runUrl = getRunUrl();
		String runFolder = getRunFolder();

		// To be improved
		{
			CucumberReportParser parser = new CucumberReportParser(runFolder);
			if (parser.tryParse()) {
				return parser.getFormatter().getReport(runUrl);
			}
		}

		{
			JunitReportParser parser = new JunitReportParser(runFolder);
			if (parser.tryParse()) {
				return parser.getFormatter().getReport(runUrl);
			}
		}

		try {
			return tryLogs();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private String tryLogs() throws IOException {
		LogExtractor extractor = new LogExtractor(run.getLogReader(), MAX_LOG_ENTRIES);
		String logSummary = extractor.extract();
		return logSummary;
	}

	private String getRunUrl() {
		String rootUrl = Jenkins.getInstanceOrNull().getRootUrl();
		String runRelativeUrl = run.getUrl();
		return rootUrl + "/" + runRelativeUrl;
	}

	private String getRunFolder() {
		return run.getRootDir().getAbsolutePath();
	}


	static String truncate(String longString, int lines) {
		int index = 0;
		int endIndex = 0;

		while (index < lines) {
			int nextIndex = longString.indexOf('\n', endIndex);
			if (nextIndex < 0) {
				break;
			}
			endIndex = nextIndex + 1;
			index++;
		}

		if (index == lines) {
			return longString.substring(0, endIndex);
		} else {
			return longString;
		}
	}

}
