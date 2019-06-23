package c8y.jenkins.hangouts.report.junit;

import c8y.jenkins.hangouts.chat.Link;
import com.google.common.collect.Lists;
import hudson.model.Run;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;

import java.io.IOException;
import java.util.LinkedList;

import static org.apache.commons.lang.StringUtils.abbreviate;
import static org.apache.commons.lang.StringUtils.substringBefore;

public class TestResultFormatter {
    private final TestResult root;
    private final Run<?, ?> build;

    public TestResultFormatter(Run<?, ?> build, TestResult root) {
        this.build = build;
        this.root = root;
    }

    public void writeTo(Appendable report) throws IOException {
        final LinkedList<TestResult> stack = linkedListOf(root);
        while (!stack.isEmpty()) {
            final TestResult result = stack.poll();
            if (isSingleTest(result)) {
                report.append(Link.of(urlOf(result), displayName(result)).toString()).append(": ").append(truncate(result.getErrorDetails())).append('\n');
            } else {
                report.append(result.getDisplayName()).append(' ').append(result.getFullName()).append('\n');
                stack.addAll(result.getFailedTests());
            }
        }
    }

    private String truncate(String message) {
        return abbreviate(substringBefore(message,"\n"), 100);
    }

    private <T> LinkedList<T> linkedListOf(T root) {
        final LinkedList<T> stack = Lists.newLinkedList();
        stack.add(root);
        return stack;
    }

    private boolean isSingleTest(TestResult result) {
        return result.getFailedTests() == null || result.getFailedTests().isEmpty() // no sub results
                || result instanceof CaseResult // or is CaseResult
                || result.getFailedTests().size() == 1 && result.getFailedTests().contains(result); // or has single self contained sub result
    }

    private String urlOf(TestResult result) {
        return (build.getAbsoluteUrl() + resolveParentAction(result).getTestResultPath(result)).replaceAll("\\s","%20");
    }

    private AbstractTestResultAction resolveParentAction(TestResult result) {
        while (result.getParentAction() == null) {
            if (result.getParent() == null || !(result.getParent() instanceof TestResult)) {
                throw new IllegalStateException("Can't resolve parent action for " + result + " to generate url");
            }
            result = (TestResult) result.getParent();
        }

        return result.getParentAction();
    }

    private String displayName(TestResult result) {
        return shortenedClassname(result.getFullDisplayName());
    }


    public String shortenedClassname(String className) {

        String[] packages = className.split("[.]");
        if (className.contains(" ") || packages.length < 2) {
            return className;
        }


        StringBuilder builder = new StringBuilder();
        int last = packages.length - 2;
        for (int i = 0; i < last; i++) {
            builder.append(packages[i].charAt(0));
            builder.append('.');
        }
        builder.append(packages[last]).append('.').append(packages[last + 1]);

        return builder.toString();
    }
}
