package c8y.jenkins.hangouts.report;

import c8y.jenkins.hangouts.chat.ChatMember;
import com.google.common.collect.ImmutableList;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.User;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static hudson.model.Result.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class RunReporterTest {
    @Mock
    AbstractProject job;

    @Mock
    Supplier<Set<ChatMember>> chatMembersSupplier;

    @Mock
    AbstractBuild build;

    AbstractBuild previousBuild;

    @Mock
    AbstractTestResultAction testResult;

    @Mock
    AbstractTestResultAction previousTestResult;

    @InjectMocks
    RunReporter reporter;
    private Set<ChatMember> chatMembers = new HashSet<>();
    private Set<User> culprits = new HashSet<>();

    @Before
    public void prepare() throws IOException {

        previousBuild = mock(AbstractBuild.class);
        given(previousBuild.getAction(AbstractTestResultAction.class)).willReturn(previousTestResult);

        given(build.getAction(AbstractTestResultAction.class)).willReturn(testResult);
        given(build.getParent()).willReturn(job);
        given(build.getCulprits()).willReturn(culprits);
        given(build.getDurationString()).willReturn("30 seconds");
        given(build.getLogReader()).willThrow(new IOException("No log"));
        given(job.getFullDisplayName()).willReturn("Test Job");
        given(chatMembersSupplier.get()).willReturn(chatMembers);
    }

    @Test
    public void shouldSkipReportWhenBuildIsSuccess() {
        //given
        given(build.getResult()).willReturn(SUCCESS);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isNotPresent();
    }

    @Test
    public void shouldGenerateReportForFirstFailedJobWithoutPrevious() {
        //given
        given(build.getResult()).willReturn(FAILURE);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent().get().asString().isNotBlank();
    }

    @Test
    public void shouldGenerateReportForFirstFailedJob() {
        //given
        given(build.getResult()).willReturn(FAILURE);
        given(build.getPreviousCompletedBuild()).willReturn(previousBuild);
        given(previousBuild.getResult()).willReturn(SUCCESS);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent().get()
                .asString()
                .contains("I am afraid the build of *Test Job* broke after 30 seconds");
    }

    @Test
    public void shouldGenerateReportForFirstUnstableJob() {
        //given
        given(build.getResult()).willReturn(UNSTABLE);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent().get()
                .asString()
                .contains("I am afraid the build of *Test Job* broke after 30 seconds");
    }

    @Test
    public void shouldIncludeCulpritsIntoBuildFailureMessage() {
        //given
        given(build.getResult()).willReturn(FAILURE);
        givenUser("Some Guy");
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent()
                .get()
                .asString()
                .contains("Would these ladies and gentlemen please take action: <Some Guy>");
    }

    private User givenUser(String username) {
        User user = mock(User.class);
        given(user.getFullName()).willReturn(username);
        culprits.add(user);
        ChatMember member = mock(ChatMember.class);
        given(member.getName()).willReturn(username);
        given(member.getDisplayName()).willReturn(username);
        chatMembers.add(member);
        return user;
    }


    @Test
    public void shouldNotGenerateReportForSecondFailedJob() {
        //given
        given(build.getResult()).willReturn(FAILURE);
        given(build.getPreviousCompletedBuild()).willReturn(previousBuild);
        given(previousBuild.getResult()).willReturn(FAILURE);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isNotPresent();
    }

    @Test
    public void shouldGenerateReportWhenBuildIsBackToNormal() {
        //given
        given(build.getResult()).willReturn(SUCCESS);
        given(build.getPreviousCompletedBuild()).willReturn(previousBuild);

        AbstractBuild lastSuccess = mock(AbstractBuild.class);
        given(lastSuccess.getNextBuild()).willReturn(previousBuild);

        given(previousBuild.getStartTimeInMillis()).willReturn(0l);
        given(previousBuild.getDuration()).willReturn(TimeUnit.SECONDS.toMillis(30));
        given(build.getStartTimeInMillis()).willReturn(TimeUnit.SECONDS.toMillis(120));
        given(previousBuild.getDuration()).willReturn(TimeUnit.SECONDS.toMillis(30));

        given(build.getPreviousSuccessfulBuild()).willReturn(lastSuccess);

        given(previousBuild.getResult()).willReturn(FAILURE);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent().get().asString().contains("The *Test Job is back to the living after 1 min 30 sec");
    }

    @Test
    public void shouldGenerateReportWhenBuildIsBackToNormalAndThereIsNoPreviousBuild() {
        //given
        given(build.getResult()).willReturn(SUCCESS);
        given(build.getPreviousCompletedBuild()).willReturn(previousBuild);
        given(previousBuild.getResult()).willReturn(FAILURE);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent().get().asString().contains("The *Test Job is back to the living");
    }

    @Test
    public void shouldGenerateReportWhenResultIsGettingWorseJob() {
        //given
        given(build.getResult()).willReturn(FAILURE);
        given(build.getPreviousCompletedBuild()).willReturn(previousBuild);
        given(previousBuild.getResult()).willReturn(UNSTABLE);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent().get().asString().contains("Result is getting worse for *Test Job*.");
    }


    @Test
    public void shouldGenerateReportWhenNumberOfFailedTestsIncreased() {
        //given
        given(build.getResult()).willReturn(FAILURE);
        given(build.getPreviousCompletedBuild()).willReturn(previousBuild);
        given(previousBuild.getResult()).willReturn(FAILURE);
        given(testResult.getFailCount()).willReturn(100);
        given(previousTestResult.getFailCount()).willReturn(5);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent().get().asString().contains("Result is getting worse for *Test Job*.");
    }

    @Test
    public void shouldGenerateReportWhenFailedTestsHasChanged() {
        //given
        given(build.getResult()).willReturn(FAILURE);
        given(build.getPreviousCompletedBuild()).willReturn(previousBuild);
        given(previousBuild.getResult()).willReturn(FAILURE);
        given(testResult.getFailCount()).willReturn(1);
        final ImmutableList<TestResult> failures = ImmutableList.of(failedTest("test-1"));
        given(testResult.getFailedTests()).willReturn(failures);
        given(previousTestResult.getFailCount()).willReturn(1);
        final ImmutableList<TestResult> previousFailures = ImmutableList.of(failedTest("test-2"));
        given(previousTestResult.getFailedTests()).willReturn(previousFailures);
        //when
        final Optional<String> report = reporter.report();
        //then
        assertThat(report).isPresent().get().asString().contains("Result is getting worse for *Test Job*.");
    }

    private TestResult failedTest(String id) {
        TestResult result = mock(TestResult.class);
        given(result.getSafeName()).willReturn(id);
        given(result.getId()).willCallRealMethod();
        return result;
    }
}
