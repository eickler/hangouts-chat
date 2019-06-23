package c8y.jenkins.hangouts.chat;

import java.util.Set;


/**
 * A simple chat room API.
 *
 * @author eickler
 */
public interface Chatroom {
    void send( String text) ;

    Set<ChatMember> getMembers();
}
