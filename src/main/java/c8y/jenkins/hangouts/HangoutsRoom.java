package c8y.jenkins.hangouts;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.chat.v1.model.ListMembershipsResponse;
import com.google.api.services.chat.v1.model.Membership;
import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.User;

@Component
public class HangoutsRoom {
	public static final String SPACE_URL = "https://chat.googleapis.com/v1/spaces/";
	public static final String MESSAGE_URL = "/messages";
	public static final String MEMBERS_URL = "/members";

	private GenericUrl messageUrl;
	private GenericUrl membersUrl;
	private JacksonFactory factory = new JacksonFactory();

	public void setRoom(String roomId) {
		this.messageUrl = new GenericUrl(SPACE_URL + roomId + MESSAGE_URL);
		this.membersUrl = new GenericUrl(SPACE_URL + roomId + MEMBERS_URL);		
	}
	
	public void send(String text) throws IOException {
		try {
			Message message = new Message();
			message.setText(text);
			JsonHttpContent json = new JsonHttpContent(factory, message);
			HttpRequest request = RequestFactory.postRequest(messageUrl, json);
			request.execute();
		} catch (IOException | GeneralSecurityException e) {
			throw new IOException(e);
		}
	}

	public Set<User> getMembers() throws IOException {
		Set<User> result = new HashSet<User>();

		try {
			HttpRequest request = RequestFactory.getRequest(membersUrl);
			request.setParser(new JsonObjectParser(factory));
			HttpResponse response = request.execute();
			ListMembershipsResponse memberships = response.parseAs(ListMembershipsResponse.class);
			for (Membership member : memberships.getMemberships()) {
				result.add(member.getMember());
			}
		} catch (IOException | GeneralSecurityException e) {
			// Ignoring errors ... the only consequence is that we cannot mention members.
		}

		return result;
	}	
}
