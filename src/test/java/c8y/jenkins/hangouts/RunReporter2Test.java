package c8y.jenkins.hangouts;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import hudson.model.CauseAction;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Cause.UserIdCause;
import hudson.model.queue.QueueTaskFuture;
import hudson.tasks.Builder;
import hudson.tasks.Shell;

@RunWith(MockitoJUnitRunner.class)
public class RunReporter2Test {
	@Rule
	public JenkinsRule j = new JenkinsRule();

	@Mock
	HangoutsRoom hangoutsRoom;

	@InjectMocks
	HangoutsBuilder hangoutsBuilder = new HangoutsBuilder("AAAAowtzMMY");

	Builder failingBuilder = new Shell("exit -1");

	@ClassRule
	public static BuildWatcher bw = new BuildWatcher();

	@Test
	public void basicFreestyleTest() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		project.getBuildersList().add(failingBuilder);
		project.getPublishersList().add(hangoutsBuilder);

		QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0, new UserIdCause());

		assertEquals(Result.FAILURE, build.get().getResult());
		verify(hangoutsRoom).send("I am afraid the build of *test0* broke. \n");
	}
/*
	@Test
	public void basicPipelineTest() throws Exception {
		String simplePipeline = FileTools.getReferenceResult("simple.pipeline");

		WorkflowJob project = j.createProject(WorkflowJob.class);
		project.setDefinition(new CpsFlowDefinition(simplePipeline, true));

		QueueTaskFuture<WorkflowRun> build = project.scheduleBuild2(0, new CauseAction(new UserIdCause()));

		//assertEquals(Result.FAILURE, build.get().getResult());
		verify(hangoutsRoom).send("I am afraid the build of *test0* broke. \n");
	}
*/
}
