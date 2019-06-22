package c8y.jenkins.hangouts.report.logs;

import com.google.common.collect.ImmutableList;
import hudson.model.Run;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static c8y.jenkins.hangouts.FileTools.load;
import static c8y.jenkins.hangouts.FileTools.openReader;
import static com.google.common.base.Charsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class LogsReportProviderTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Mock
    Run<?, ?> build;

    LogsReportProvider provider;

    @Before
    public void create() {
        provider = new LogsReportProvider(build, 500);
    }

    @Test
    public void shouldNotBeAbleToProvideLogsWhenGetLogsReaderFailsWithException() throws IOException {
        //given
        given(build.getLogReader()).willThrow(new IOException("No log"));

        //when
        final boolean canProvide = provider.canProvide();
        //then
        assertThat(canProvide).isFalse();
    }

    @Test
    public void shouldBeAbleToProvideLogsReportWhenLogReaderIsNotNull() throws IOException {
        //given
        given(build.getLogReader()).will(returnLogsFrom("report/logs/failed-maven-build.log"));

        //when
        final boolean canProvide = provider.canProvide();
        //then
        assertThat(canProvide).isTrue();
    }


    @Test
    public void shouldProvideReportForFailedBuild() throws IOException {
        final SoftAssertions soft = new SoftAssertions();
        for (String file : ImmutableList.of("failed-maven", "failed-plain")) {
            //given
            givenLogs(file);

            //when
            final String report = provider.report();
            //then
            soft.assertThat(report).isNotBlank().isEqualTo(load("report/logs/" + file + "-expected.log"));
        }
        soft.assertAll();
    }

    private void givenLogs(String file) throws IOException {
        final String logs = "report/logs/" + file + "-build.log";
        given(build.getLogReader()).will(returnLogsFrom(logs));
        given(build.getCharset()).willReturn(UTF_8);
        given(build.getLogFile()).will(returnLogFileFrom(logs));
        given(build.getLog(anyInt())).willCallRealMethod();

    }

    private Answer<?> returnLogFileFrom(String logs) {
        return (i) -> {
            try {
                final File logFile = temp.newFile();
                Files.write(logFile.toPath(), load(logs).getBytes());
                return logFile;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Answer<?> returnLogsFrom(String logs) {
        return (i) -> openReader(logs);
    }


}
