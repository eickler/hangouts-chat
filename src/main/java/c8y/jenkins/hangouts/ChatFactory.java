package c8y.jenkins.hangouts;

public class ChatFactory {
	private static Chatroom chatroom;
	
	public static Chatroom get() {
		if (chatroom == null) {
			chatroom = new HangoutsChatroom();
		}
		return chatroom;
	}
}
