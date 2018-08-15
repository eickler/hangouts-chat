package c8y.jenkins.hangouts;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.google.api.services.chat.v1.model.User;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

/**
 * A post-build action for notifying a chat room about a failed build. See also {@link HangoutsStep}.
 * 
 * @author eickler
 *
 */
public class HangoutsBuilder extends Notifier {

	public static final int MAX_ENTRIES = 10;

	private String room;

	@DataBoundConstructor
	public HangoutsBuilder(String room) {
		this.room = room;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		Result result = build.getResult();
		if (result != Result.SUCCESS) {
			sendBrokenBuildMessage(build);
		}
		return true;
	}

	private void sendBrokenBuildMessage(AbstractBuild<?, ?> build) throws IOException {
		Chatroom chatroom = ChatFactory.get();
		Set<User> members = chatroom.getMembers(room);

		RunReporter reporter = new RunReporter(build);
		String report = reporter.report(members);

		if (report != null && report.length() > 0) {
			chatroom.send(room, report);
		}
	}

	@Symbol("chat")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		public FormValidation doCheckRoom(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error(Messages.HangoutsBuilder_DescriptorImpl_errors_missingRoom());
			return FormValidation.ok();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.HangoutsBuilder_DescriptorImpl_DisplayName();
		}
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

}
