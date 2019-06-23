package c8y.jenkins.hangouts.report;

import c8y.jenkins.hangouts.ReportProvider;
import c8y.jenkins.hangouts.chat.ChatMember;
import c8y.jenkins.hangouts.report.junit.TestReportProvider;
import c8y.jenkins.hangouts.report.logs.LogsReportProvider;
import com.google.common.collect.ImmutableList;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.User;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

import static c8y.jenkins.hangouts.report.Reports.dropCharacters;
import static c8y.jenkins.hangouts.report.Reports.truncate;
import static hudson.model.Result.SUCCESS;

/**
 * Produce a short and polite report about a build failure.
 *
 * @author eickler
 */
public class RunReporter {
    public static final int MAX_LOG_ENTRIES = 20;

    private Run<?, ?> run;
    private final Supplier<Set<ChatMember>> chatMembers;

    public RunReporter(Run<?, ?> run, Supplier<Set<ChatMember>> chatMembers) {
        this.run = run;
        this.chatMembers = chatMembers;
    }

    public Optional<String> report() {
        if (isBackToNormal()) {
            return Optional.of(reportBackToNormal());
        } else if (isSuccess()) {
            return Optional.empty();// Optional.of(reportSuccess()); TODO make it configurable
        } else if (isFirstFailure()) {
            return Optional.of(reportFailure());
        } else if (isWorse()) {
            return Optional.of(reportGettingWorse());
        }
        return Optional.empty();
    }

    private String reportGettingWorse() {
        StringBuilder buffer = new StringBuilder();
        buffer
//                .append(selectRandomOf("I think that's the right moment to  " + Link.of("http://www.nooooooooooooooo.com/", "press it"),
//                "Please stop, you are not helping.",
//                "No comment here."))
//                .append('\n')
                .append("Result is getting worse for *").append(getJob()).append("*.");
        appendFailure(buffer);
        return buffer.toString();
    }

    private boolean isSuccess() {
        return run.getResult() == SUCCESS;
    }


    private Run<?, ?> getPreviousRun() {
        Run<?, ?> previousBuild = run;
        do {
            previousBuild = previousBuild.getPreviousCompletedBuild();
        } while (previousBuild != null && previousBuild.getResult() == Result.ABORTED);
        return previousBuild;
    }

    @Nonnull
    private Result getPreviousResult() {
        final Run<?, ?> previousRun = getPreviousRun();
        if (previousRun == null) {
            return SUCCESS;
        }
        return getResult(previousRun);
    }

    private Result getResult(Run<?, ?> previousRun) {
        final Result result = previousRun.getResult();
        return result == null ? SUCCESS : result;
    }

    private boolean isBackToNormal() {
        return isSuccess() && getPreviousResult() != SUCCESS;
    }

    private boolean isWorse() {
        final Result current = getResult(run);
        return current.isWorseThan(getPreviousResult()) || isFailedWithoutTests() || isNumberOfFailedTestsIncreasedOrChanged();
    }

    private boolean isNumberOfFailedTestsIncreasedOrChanged() {
        final Run<?, ?> previousRun = getPreviousRun();
        final AbstractTestResultAction previousTestResult = getTestResult(previousRun);
        if (getTestResult(run) != null && previousTestResult != null) {
            if (getTestResult(run).getFailCount() > previousTestResult.getFailCount())
                return true;
            // test if different tests failed.
            return !getFailedTestIds(run).equals(getFailedTestIds(previousRun));
        }
        return false;
    }

    private Set<String> getFailedTestIds(Run<?, ?> currentBuild) {
        Set<String> failedTestIds = new HashSet<>();
        List<? extends TestResult> failedTests = getTestResult(currentBuild).getFailedTests();
        for (TestResult result : failedTests) {
            failedTestIds.add(result.getId());
        }

        return failedTestIds;
    }

    private AbstractTestResultAction getTestResult(Run<?, ?> build) {
        return build != null ? build.getAction(AbstractTestResultAction.class) : null;
    }

    private boolean isFailedWithoutTests() {
        final Run<?, ?> previousBuild = run.getPreviousBuild();
        return previousBuild != null && previousBuild.getAction(AbstractTestResultAction.class) != null && run.getAction(AbstractTestResultAction.class) == null;
    }


    private boolean isFirstFailure() {
        return run.getResult() != SUCCESS && getPreviousResult() == SUCCESS;
    }

//    private String reportSuccess() {
//        StringBuilder buffer = new StringBuilder();
//        buffer.append("The build of *")
//                .append(getJob())
//                .append("* has finished successfully after ")
//                .append(run.getDurationString())
//                .append('.');
//        return buffer.toString();
//    }

    private String reportFailure() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("I am afraid the build of *");
        buffer.append(getJob());
        buffer.append("* broke after ")
                .append(run.getDurationString())
                .append('.');
        appendFailure(buffer);
        return buffer.toString();
    }

    private void appendFailure(StringBuilder buffer) {
        Set<ChatMember> members = chatMembers.get();
        Set<String> culprits = getCulprits(members);
        if (culprits.size() > 0) {
            buffer.append("Would these ladies and gentlemen please take action: ");

            for (String culprit : culprits) {
                buffer.append(culprit);
                buffer.append(" ");
            }
        }
        buffer.append("\n");

        // We need to probably do a better job in telling the people how many items
        // there are and if the log was truncated.
        String logSummary = truncate(failureReport(), MAX_LOG_ENTRIES);
        buffer.append(logSummary);
        dropCharacters(buffer, '"');
    }


    private String getJob() {
        return run.getParent().getFullDisplayName();
    }

    private Set<String> getCulprits(Set<ChatMember> members) {
        Set<String> result = getCulprits();

        for (ChatMember user : members) {
            if (result.contains(user.getDisplayName())) {
                result.remove(user.getDisplayName());
                result.add("<" + user.getName() + ">");
            }
        }

        return result;
    }

    @Nonnull
    private Set<String> getCulprits() {
        Set<String> result = new HashSet<>();
        Set<User> users;

        if (run instanceof WorkflowRun) {
            // From Jenkins Pipeline
            users = ((WorkflowRun) run).getCulprits();
        } else if (run instanceof AbstractBuild<?, ?>) {
            // From normal Jobs
            users = ((AbstractBuild<?, ?>) run).getCulprits();
        } else {
            users = new HashSet<>();
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

    private String failureReport() {
        for (ReportProvider provider : getProviders()) {
            if (provider.canProvide()) {
                return provider.report();
            }
        }
        return "";
    }

    private Iterable<? extends ReportProvider> getProviders() {
        return ImmutableList.of(new TestReportProvider(run),
                new LogsReportProvider(run, MAX_LOG_ENTRIES));
    }

    @Nonnull
    private String reportBackToNormal() {
        final String duration = createBackToNormalDurationString();
        return selectRandomOf("Congratulations.",
                "Someone should get a beer !!!",
                "Good Job!",
                ":thumbup",
                " It's alive !!!") + "\n The *" + getJob() + " is back to the living" + (StringUtils.isBlank(duration) ? "" : " after " + duration);
    }

    private String createBackToNormalDurationString() {
        // This status code guarantees that the previous build fails and has been successful before
        // The back to normal time is the time since the build first broke
        Run previousSuccessfulBuild = run.getPreviousSuccessfulBuild();
        if (null != previousSuccessfulBuild && null != previousSuccessfulBuild.getNextBuild()) {
            Run initialFailureAfterPreviousSuccessfulBuild = previousSuccessfulBuild.getNextBuild();
            if (initialFailureAfterPreviousSuccessfulBuild != null) {
                long initialFailureStartTime = initialFailureAfterPreviousSuccessfulBuild.getStartTimeInMillis();
                long initialFailureDuration = initialFailureAfterPreviousSuccessfulBuild.getDuration();
                long initialFailureEndTime = initialFailureStartTime + initialFailureDuration;
                long buildStartTime = run.getStartTimeInMillis();
                long buildDuration = run.getDuration();
                long buildEndTime = buildStartTime + buildDuration;
                long backToNormalDuration = buildEndTime - initialFailureEndTime;
                return Util.getTimeSpanString(backToNormalDuration);
            }
        }
        return null;
    }


    private String selectRandomOf(String... words) {
        Random rand = new Random();
        return words[rand.nextInt(words.length)];
    }
}
