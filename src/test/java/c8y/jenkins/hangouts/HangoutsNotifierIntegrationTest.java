package c8y.jenkins.hangouts;

import c8y.jenkins.hangouts.chat.ChatFactory;
import c8y.jenkins.hangouts.chat.HangoutsChatroom;
import hudson.model.Cause.UserIdCause;
import hudson.model.CauseAction;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.*;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static c8y.jenkins.hangouts.chat.ChatFactory.setFactory;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HangoutsNotifierIntegrationTest {

    public static final String ROOM_ID = "AAAAowtzMMY";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Mock
    HangoutsChatroom hangoutsRoom;

    Builder failingBuilder = new Shell("exit -1");

    @ClassRule
    public static BuildWatcher bw = new BuildWatcher();

    @Before
    public void setup() {
        setFactory((room) -> hangoutsRoom);
    }

    @After
    public void cleanup() {
        ChatFactory.reset();
    }

    @Test
    public void basicFreestyleTest() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.getBuildersList().add(failingBuilder);
        project.getPublishersList().add(new HangoutsNotifier(ROOM_ID));

        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0, new UserIdCause());

        assertEquals(Result.FAILURE, build.get().getResult());
        verify(hangoutsRoom).send(contains("I am afraid the build of *test0* broke"));
    }

    @Test
    public void basicPipelineTest() throws Exception {
        String simplePipeline = FileTools.load("simple.pipeline");

        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(simplePipeline, true));

        project.scheduleBuild2(0, new CauseAction(new UserIdCause())).get();

        verify(hangoutsRoom).send(contains("I am afraid the build of *test0* broke"));
    }


    @Test
    public void basicFolderTest() throws Exception {

        final MockFolder parent = j.createFolder("parent");
        FreeStyleProject project = parent.createProject(FreeStyleProject.class, "subproject");
        project.getBuildersList().add(failingBuilder);
        project.getPublishersList().add(new HangoutsNotifier(ROOM_ID));


        project.scheduleBuild2(0, new CauseAction(new UserIdCause())).get();

        verify(hangoutsRoom).send(contains("I am afraid the build of *parent » subproject* broke"));
    }
}
