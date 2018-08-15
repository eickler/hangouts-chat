package c8y.jenkins.hangouts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;

public class RequestFactory {
	private static final List<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/chat.bot");
	private static final String PATH = "hangouts_api.json";

	private static HttpRequestFactory factory;

	public static HttpRequestFactory instance() throws IOException, GeneralSecurityException {
		if (factory == null) {
			try (InputStream is = RequestFactory.class.getClassLoader().getResourceAsStream(PATH)) {
				GoogleCredential credential = GoogleCredential.fromStream(is).createScoped(SCOPE);
				HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
				factory = httpTransport.createRequestFactory(credential);
			}
		}
		return factory;
	}

	public static HttpRequest getRequest(GenericUrl url)
			throws FileNotFoundException, IOException, GeneralSecurityException {
		return instance().buildGetRequest(url);
	}

	public static HttpRequest postRequest(GenericUrl url, HttpContent content)
			throws FileNotFoundException, IOException, GeneralSecurityException {
		return instance().buildPostRequest(url, content);
	}
}
