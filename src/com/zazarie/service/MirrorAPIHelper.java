package com.zazarie.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Subscription;
import com.google.appengine.api.datastore.Entity;
import com.zazarie.domain.GlassRedditCredentialStore;
import com.zazarie.domain.GoogleOauthSession;
import com.zazarie.mvc.AuthUtil;

@Component
public class MirrorAPIHelper {

	private static final Logger LOG = Logger.getLogger(MirrorAPIHelper.class.getSimpleName());
	
	private GlassRedditCredentialStore store;
	
	private AuthUtil authUtil;

	/**
	 * Subscribes to notifications on the user's timeline.
	 */
	public static Subscription insertSubscription(Credential credential, String callbackUrl,
			String userId, String collection) throws IOException {
		
		LOG.info("Attempting to subscribe verify_token " + userId + " with callback " + callbackUrl);

		// Rewrite "appspot.com" to "Appspot.com" as a workaround for
		// http://b/6909300.
		callbackUrl = callbackUrl.replace("appspot.com", "Appspot.com");

		Subscription subscription = new Subscription();
		// Alternatively, subscribe to "locations"
		subscription.setCollection(collection);
		subscription.setCallbackUrl(callbackUrl);
		subscription.setUserToken(userId);

		return getMirror(credential).subscriptions().insert(subscription).execute();
	}

	public static Mirror getMirror(Credential credential) {
		return new Mirror.Builder(new UrlFetchTransport(), new JacksonFactory(), credential).setApplicationName("Reddit for Glass").build();
	}
	
	public static Credential getCredential(GoogleOauthSession googleOauthSession) {
		HttpTransport httpTransport = new UrlFetchTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		
		//Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
		
		Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
	    	.setJsonFactory(jsonFactory)
	    	.setTransport(httpTransport)
	    	.build();
		
		credential.setAccessToken(googleOauthSession.getAccessToken());
		
		return credential;
	}
	
	public static Credential getCredentialFromToken(String accessToken) {
		HttpTransport httpTransport = new UrlFetchTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		
		//Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
		
		Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
	    	.setJsonFactory(jsonFactory)
	    	.setTransport(httpTransport)
	    	.build();
		
		credential.setAccessToken(accessToken);
		
		return credential;
	}
	
	public Entity checkOauthToken(String userId, HttpServletRequest request) {
		
		// check to see whether a user entry already exists
		Entity entity = store.load(userId);

		if (entity != null) {
			// check if access token has expired, if so, refresh it
			long expires = (Long) entity.getProperty("expirationTimeMillis");
			
			if (new Date().getTime() >= expires) {
				
				// refresh the access token
				try {
					LOG.info("Refreshing access token for Google, authUtil is: " + userId);
					
					authUtil.getCredential(userId).refreshToken();
					
					// unfortunately, after we refresh the token, the entry is removed/re-added by google, so we need to re-populate the entity with the other attributes
					
					Entity newEntity = store.load(userId);
					
					newEntity.setProperty("email", entity.getProperty("email"));
					newEntity.setProperty("redditAccessToken", entity.getProperty("redditAccessToken"));
					newEntity.setProperty("redditRefreshToken", entity.getProperty("redditRefreshToken"));
					newEntity.setProperty("redditExpires", entity.getProperty("redditExpires"));
					
					store.update(newEntity);
					
					return entity;
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				LOG.info("No need to refresh access token");
			}		
			
			return entity;
		}
			
		return null;
	}
	
	public static byte[] getImageBytesFromURL(URL url) throws Exception {
		InputStream is = null;

		byte[] imageBytes = null;

		try {
			is = url.openStream ();
			imageBytes = IOUtils.toByteArray(is);
		}
		catch (IOException e) {
			System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
			e.printStackTrace ();
			// Perform any other exception handling that's appropriate.
		}
		finally {
			if (is != null) { is.close(); }
		}

		return imageBytes;
	}
	
	public GlassRedditCredentialStore getStore() {
		return store;
	}

	@Autowired(required=true)
	public void setStore(GlassRedditCredentialStore store) {
		this.store = store;
	}
	
	public AuthUtil getAuthUtil() {
		return authUtil;
	}

	@Autowired(required=true)
	public void setAuthUtil(AuthUtil authUtil) {
		this.authUtil = authUtil;
	}
}
