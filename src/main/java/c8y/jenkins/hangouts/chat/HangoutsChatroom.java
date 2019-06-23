package c8y.jenkins.hangouts.chat;

import c8y.jenkins.hangouts.RequestFactory;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.chat.v1.model.ListMembershipsResponse;
import com.google.api.services.chat.v1.model.Membership;
import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.Thread;
import com.google.api.services.chat.v1.model.User;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * A hangouts chat implementation of the simple Chatroom API.
 *
 * @author eickler
 */
public class HangoutsChatroom implements Chatroom {
    public static final String SPACE_URL = "https://chat.googleapis.com/v1/spaces/";
    public static final String MESSAGE_URL = "/messages";
    public static final String MEMBERS_URL = "/members";

    private static JacksonFactory factory = new JacksonFactory();

    private final String roomId;

    public HangoutsChatroom(String roomId) {
        this.roomId = roomId;
    }

    public void send(String text) {
        try {
            Message message = new Message();
            message.setText(text);
            JsonHttpContent jsonMessage = new JsonHttpContent(factory, message);
            GenericUrl messageUrl = new GenericUrl(SPACE_URL + roomId + MESSAGE_URL);
            HttpRequest request = RequestFactory.postRequest(messageUrl, jsonMessage);
            request.execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Send message to room " + roomId + " has failed", e);
        }
    }

    public Set<ChatMember> getMembers() {

        try {
            GenericUrl membersUrl = new GenericUrl(SPACE_URL + roomId + MEMBERS_URL);
            HttpRequest request = RequestFactory.getRequest(membersUrl);
            request.setParser(new JsonObjectParser(factory));
            HttpResponse response = request.execute();

            ListMembershipsResponse memberships = response.parseAs(ListMembershipsResponse.class);

            return memberships.getMemberships().stream().map(this::asUser).collect(toSet());

        } catch (IOException | GeneralSecurityException e) {
            // Ignoring errors ... the only consequence is that we cannot mention members.
        }
        return Collections.emptySet();
    }

    private ChatMember asUser(Membership membership) {
        final User member = membership.getMember();
        return new HangoutsChatMember(member.getDisplayName(), member.getName());
    }

}
