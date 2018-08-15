package c8y.jenkins.hangouts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jenkinsci.plugins.plaincredentials.FileCredentials;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;

import hudson.security.ACL;
import jenkins.model.Jenkins;

/**
 * A helper class to get credentials from Jenkins and operate the Hangouts Chat
 * REST API. Note that I chose to not depends on the Google OAuth plugin, since
 * it does not add a lot of new functionality but introduces a dependency on an
 * older version of the Google APIs.
 * 
 * @author eickler
 *
 */
public class RequestFactory {
	public static final String CREDS_ID = "Hangouts";
	private static final List<String> SCOPE = Arrays.asList("https://www.googleapis.com/auth/chat.bot");

	private static HttpRequestFactory factory;

	public static HttpRequestFactory instance() throws IOException, GeneralSecurityException {
		if (factory == null) {
			instantiate();
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

	private static void instantiate() throws IOException, GeneralSecurityException {
		try (InputStream is = getFileCredentialsFromJenkins()) {
			GoogleCredential credential = GoogleCredential.fromStream(is).createScoped(SCOPE);
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			factory = httpTransport.createRequestFactory(credential);
		}
	}

	private static InputStream getFileCredentialsFromJenkins() throws IOException {
		List<FileCredentials> fileCredentials = CredentialsProvider.lookupCredentials(FileCredentials.class,
				Jenkins.getInstanceOrNull(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList());

		for (FileCredentials cred : fileCredentials) {
			if (cred.getId().equals(CREDS_ID)) {
				return cred.getContent();
			}
		}
		throw new IOException("Credentials not found. Please add your credentials file to Jenkins using the ID \"Hangouts\".");
	}
}
