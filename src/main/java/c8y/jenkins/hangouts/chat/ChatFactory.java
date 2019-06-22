package c8y.jenkins.hangouts.chat;

import java.util.function.Function;

/**
 * A "poor man's factory", as I have not been able to get Guice or Spring to
 * cooperate with Jenkins Steps. So I reverted to this approach and used
 * PowerMock.
 *
 * @author eickler
 */
public class ChatFactory {


    private static Function<String, Chatroom> factory = HangoutsChatroom::new;

    public static Chatroom get(String room) {
        return factory.apply(room);
    }

    public static void setFactory(Function<String, Chatroom> newFactory) {
        factory = newFactory;
    }

    public static void reset() {
        factory = HangoutsChatroom::new;
    }

}
