
package com.zazarie.mvc;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.appengine.api.datastore.Entity;
import com.zazarie.domain.GlassRedditCredentialStore;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class AuthUtil {
	
	public static final String GLASS_SCOPE = "https://www.googleapis.com/auth/glass.timeline "
			+ "https://www.googleapis.com/auth/glass.location "
			+ "https://www.googleapis.com/auth/userinfo.profile";
	
	private static final Logger LOG = Logger.getLogger(AuthUtil.class.getSimpleName());
	
	private @Value("${google_client_id}") String googleClientId;

	private @Value("${google_client_secret}") String googlePass;
	
	/**
	 * Creates and returns a new {@link AuthorizationCodeFlow} for this app.
	 */
	public AuthorizationCodeFlow newAuthorizationCodeFlow()
			throws IOException {

		LOG.info("googleClientId: " + googleClientId);
		
		return new GoogleAuthorizationCodeFlow.Builder(new UrlFetchTransport(),
				new JacksonFactory(), googleClientId, googlePass,
				Collections.singleton(GLASS_SCOPE)).setAccessType("offline")
				.setCredentialStore(new GlassRedditCredentialStore()).build();
	}


	/**
	 * Get the current user's ID from the session
	 * 
	 * @return string user id or null if no one is logged in
	 */
	public static String getUserId(HttpServletRequest request) {
		HttpSession session = request.getSession();
		return (String) session.getAttribute("userId");
	}

	public static void setUserId(HttpServletRequest request, String userId) {
		HttpSession session = request.getSession();
		session.setAttribute("userId", userId);
	}

	public void clearUserId(HttpServletRequest request)
			throws IOException {
		// Delete the credential in the credential store
		String userId = getUserId(request);
		
		new GlassRedditCredentialStore().delete(userId, getCredential(userId));

		// Remove their ID from the local session
		request.getSession().removeAttribute("userId");
	}

	public Credential getCredential(String userId) throws IOException {
		if (userId == null) {
			return null;
		} else {
			return newAuthorizationCodeFlow().loadCredential(userId);
		}
	}

	public static boolean isRedditLoggedIn(HttpServletRequest request) {
		GlassRedditCredentialStore store = new GlassRedditCredentialStore();
		
		if (AuthUtil.getUserId(request) == null)
			return false;

		Entity entity = store.load(AuthUtil.getUserId(request));
		
		if (entity == null)
			return false;
		
		if (entity.getProperty("redditAccessToken") != null) {
			return true;
		}
		
		return false;
	}

	public Credential getCredential(HttpServletRequest req)
			throws IOException {
		
		return newAuthorizationCodeFlow().loadCredential(getUserId(req));
	}

	public static List<String> getAllUserIds() {
		return new GlassRedditCredentialStore().listAllUsers();
	}

	public String getGoogleClientId() {
		return googleClientId;
	}

	public void setGoogleClientId(String googleClientId) {
		this.googleClientId = googleClientId;
	}

	public String getGooglePass() {
		return googlePass;
	}

	public void setGooglePass(String googlePass) {
		this.googlePass = googlePass;
	}
	
	
}
