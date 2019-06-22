package c8y.jenkins.hangouts.report.junit;

import c8y.jenkins.hangouts.ReportProvider;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;

import java.io.IOException;
import java.util.List;

public class TestReportProvider implements ReportProvider {

    private final Run<?, ?> build;

    public TestReportProvider(Run<?, ?> build) {
        this.build = build;
    }

    @Override
    public boolean canProvide() {
        final List<AbstractTestResultAction> action = build.getActions(AbstractTestResultAction.class);
        return action != null && !action.isEmpty() && hasFailedTests(action);
    }

    private boolean hasFailedTests(List<AbstractTestResultAction> actions) {
        for(AbstractTestResultAction<?> action :actions){
            if(!action.getFailedTests().isEmpty()){
                return true;
            }
        }

        return false;
    }

    @Override
    public String report() {
        StringBuilder report = new StringBuilder();
        for (final AbstractTestResultAction action : build.getActions(AbstractTestResultAction.class)) {
            final List<TestResult> failedTests = action.getFailedTests();
            report.append("Test summary passed: ").append(getPassedCount(action))
                    .append(" skipped: ").append(action.getSkipCount())
                    .append(" failed: ").append(action.getFailCount())
                    .append("\n");

            for (TestResult result : failedTests) {
                final TestResultFormatter formatter = new TestResultFormatter(build, result);
                try {
                    formatter.writeTo(report);
                } catch (IOException e) {
                }
            }
        }
        return report.toString();
    }

    private int getPassedCount(AbstractTestResultAction action) {
        return action.getTotalCount() - action.getSkipCount() - action.getFailCount();
    }
}
