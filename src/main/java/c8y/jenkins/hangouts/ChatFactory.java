package c8y.jenkins.hangouts;

/**
 * A "poor man's factory", as I have not been able to get Guice or Spring to
 * cooperate with Jenkins Steps. So I reverted to this approach and used
 * PowerMock.
 * 
 * @author eickler
 *
 */
public class ChatFactory {
	private static Chatroom chatroom;

	public static Chatroom get() {
		if (chatroom == null) {
			chatroom = new HangoutsChatroom();
		}
		return chatroom;
	}
}
