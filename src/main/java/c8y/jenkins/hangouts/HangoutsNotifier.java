package c8y.jenkins.hangouts;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;

import c8y.jenkins.hangouts.chat.ChatFactory;
import c8y.jenkins.hangouts.chat.Chatroom;
import c8y.jenkins.hangouts.report.RunReporter;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

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
public class HangoutsNotifier extends Notifier {

	private String room;

	@DataBoundConstructor
	public HangoutsNotifier(String room) {
		this.room = room;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		Result result = build.getResult();

		if (result != Result.SUCCESS) {
			notifyResult(build);
		}
		return true;
	}


	private void notifyResult(AbstractBuild<?, ?> build) throws IOException {
		Chatroom chatroom = ChatFactory.get(room);

		RunReporter reporter = new RunReporter(build, chatroom::getMembers);
		Optional<String> report = reporter.report();

		report.filter(StringUtils::isNotBlank)
				.ifPresent(chatroom::send);

	}

	@Symbol("chat")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		public FormValidation doCheckRoom(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error(Messages.HangoutsNotifier_DescriptorImpl_errors_missingRoom());
			return FormValidation.ok();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.HangoutsNotifier_DescriptorImpl_DisplayName();
		}
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

}
