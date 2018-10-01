package c8y.jenkins.hangouts;

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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.net.ssl.*"})
@PrepareForTest(ChatFactory.class)
public class RunReporter2Test {
	
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
		PowerMockito.mockStatic(ChatFactory.class);
		Mockito.when(ChatFactory.get()).thenReturn(hangoutsRoom);
	}
	
	@Test
	public void basicFreestyleTest() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		project.getBuildersList().add(failingBuilder);
		project.getPublishersList().add(new HangoutsBuilder(ROOM_ID));

		QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0, new UserIdCause());

		assertEquals(Result.FAILURE, build.get().getResult());
		verify(hangoutsRoom).send(ROOM_ID, "I am afraid the build of *test0* broke. \n");
	}

	@Test
	public void basicPipelineTest() throws Exception {
		String simplePipeline = FileTools.getReferenceResult("simple.pipeline");

		WorkflowJob project = j.createProject(WorkflowJob.class);
		project.setDefinition(new CpsFlowDefinition(simplePipeline, true));

		project.scheduleBuild2(0, new CauseAction(new UserIdCause())).get();

		verify(hangoutsRoom).send(ROOM_ID, "I am afraid the build of *test0* broke. \n");
	}

	@Test
	public void basicFolderTest() throws Exception {

		final MockFolder parent = j.createFolder("parent");
		FreeStyleProject project = parent.createProject(FreeStyleProject.class,"subproject");
		project.getBuildersList().add(failingBuilder);
		project.getPublishersList().add(new HangoutsBuilder(ROOM_ID));



		project.scheduleBuild2(0, new CauseAction(new UserIdCause())).get();

		verify(hangoutsRoom).send(ROOM_ID, "I am afraid the build of *parent » subproject* broke. \n");
	}
}
