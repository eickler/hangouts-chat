package c8y.jenkins.hangouts.report.junit;

import c8y.jenkins.hangouts.FileTools;
import com.google.common.collect.ImmutableList;
import hudson.XmlFile;
import hudson.model.Run;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.util.XStream2;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static c8y.jenkins.hangouts.FileTools.openStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class JunitReportProviderTest {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();
    @Mock
    Run build;

    @Mock
    TestResultAction testAction;

    @BeforeClass
    public static void initTestResults() throws IOException {
        final File junitReport = new File(folder.getRoot(), "junitResult.xml");
        Files.copy(openStream("junitResult.xml"), junitReport.toPath());
    }


    @Before
    public void prepare() throws IOException {

        given(build.getActions(AbstractTestResultAction.class)).willReturn(ImmutableList.of(testAction));
        given(build.getAbsoluteUrl()).willReturn("http://localhost/job/JobName/2/");
        given((testAction.getUrlName())).willReturn("testReport");
        given(testAction.getTestResultPath(ArgumentMatchers.isA(hudson.tasks.test.TestResult.class))).will((inv) -> testAction.getUrlName() +"/" + inv.<hudson.tasks.test.TestResult>getArgument(0).getRelativePathFrom(null));
        testAction.run = build;


        final XStream2 stream = new XStream2();
        stream.alias("result", TestResult.class);
        stream.alias("suite", SuiteResult.class);
        stream.alias("case", CaseResult.class);
        final File resultFile = new File(folder.getRoot(), "junitResult.xml");
        TestResult result = (TestResult) new XmlFile(stream, resultFile).read();


        result.setParentAction(testAction);


        given(testAction.getFailedTests()).willReturn(result.getFailedTests());
        given(testAction.getFailCount()).willReturn(5);
        given(testAction.getSkipCount()).willReturn(3);
        given(testAction.getTotalCount()).willReturn(10);


    }

    @Test
    public void shouldBeAbleToProvideReport() throws IOException {
        TestReportProvider reporter = new TestReportProvider(build);

        assertThat(reporter.canProvide()).isTrue();
    }

    @Test
    public void shouldBeNotAbleToProvideReportWhenTestActionNotHaveFailedTests() throws IOException {
        reset(build); // No build action
        final TestResultAction testResultWihtoutFailures = mock(TestResultAction.class);
        given(build.getActions(AbstractTestResultAction.class)).willReturn(ImmutableList.of(testResultWihtoutFailures));
        TestReportProvider reporter = new TestReportProvider(build);
        assertThat(reporter.canProvide()).isFalse();
    }

    @Test
    public void shouldNotBeAbleToProvideReport() throws IOException {
        reset(build); // No build action
        TestReportProvider reporter = new TestReportProvider(build);

        assertThat(reporter.canProvide()).isFalse();
    }


    @Test
    public void shouldGenerateReport() throws IOException {
        TestReportProvider reporter = new TestReportProvider(build);

        assertThat(reporter.report()).isEqualTo(FileTools.load("junitReference.txt"));
    }
}
