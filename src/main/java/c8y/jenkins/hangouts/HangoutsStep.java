package c8y.jenkins.hangouts;

import c8y.jenkins.hangouts.chat.ChatFactory;
import c8y.jenkins.hangouts.chat.Chatroom;
import c8y.jenkins.hangouts.report.RunReporter;
import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.Run;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * A Jenkins Pipeline step for notifying a chat room about a failed build. See also {@link HangoutsNotifier}.
 *
 * @author eickler
 */
public class HangoutsStep extends Step implements Serializable {

    private static final long serialVersionUID = -4772889830814883958L;
    private static final Logger LOGGER = Logger.getLogger(HangoutsStep.class.getName());

    private String room;
    private String message;

    @DataBoundConstructor
    public HangoutsStep(@Nonnull String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    public String getMessage() {
        return message;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, room, message);
    }

    public static class Execution extends SynchronousStepExecution<Void> {

        private static final long serialVersionUID = 6872125878600187425L;

        private String roomId;
        private String message;
        private transient Run<?, ?> run;

        protected Execution(StepContext context, String roomId, String message) {
            super(context);
            this.roomId = roomId;
            this.message = message;

            try {
                this.run = context.get(Run.class);
            } catch (IOException e) {
                LOGGER.error(e);
            } catch (InterruptedException e) {
                LOGGER.error(e);
            }
        }

        @Override
        protected Void run() {
            Chatroom chatroom = ChatFactory.get(roomId);
            if (isNotBlank(message)){
                chatroom.send(message);
            } else {
                RunReporter reporter = new RunReporter(run, chatroom::getMembers);
                Optional<String> report = reporter.report();
                report.ifPresent(chatroom::send);
            }

            return null;
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public String getFunctionName() {
            return "chat";
        }

        @Override
        public String getDisplayName() {
            return Messages.HangoutsNotifier_DescriptorImpl_DisplayName();
        }

        public FormValidation doCheckRoom(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HangoutsNotifier_DescriptorImpl_errors_missingRoom());
            if (value.length() < 4)
                return FormValidation.warning(Messages.HangoutsNotifier_DescriptorImpl_warnings_tooShort());
            return FormValidation.ok();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class);
        }
    }
}