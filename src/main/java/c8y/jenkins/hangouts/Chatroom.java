package c8y.jenkins.hangouts;

import java.io.IOException;
import java.util.Set;

import com.google.api.services.chat.v1.model.User;

public interface Chatroom {
	void send(String roomId, String text) throws IOException;
	Set<User> getMembers(String roomId) throws IOException;
}