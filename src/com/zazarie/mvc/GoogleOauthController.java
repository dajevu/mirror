package com.zazarie.mvc;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.appengine.api.datastore.Entity;
import com.zazarie.domain.GlassRedditCredentialStore;
import com.zazarie.domain.GoogleOauthSession;
import com.zazarie.service.NewUserBootstrapper;
import com.zazarie.service.WebUtil;

@Controller
public class GoogleOauthController {

	private static final Logger LOG = Logger.getLogger(MainController.class.getSimpleName());

	private @Value("${google_client_id}") String googleClientId;

	private @Value("${google_client_secret}") String googlePass;
	
	private AuthUtil authUtil;
	
	private NewUserBootstrapper newUserBootstrapper;
	
	private GlassRedditCredentialStore store;

	@RequestMapping(value = "/authorize-google", method = RequestMethod.GET)
	public String authorize(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		
		LOG.info("Inside authorize...");
		
		LOG.info("authUtil is: " + authUtil.getGoogleClientId());

		// If something went wrong, log the error message.
		if (request.getParameter("error") != null) {
			
			LOG.severe("Something went wrong during auth: " + request.getParameter("error"));
			
			response.setContentType("text/plain");
			
			try {
				response.getWriter().write("Something went wrong during auth. Please check your log for details");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		// If we have a code, finish the OAuth 2.0 dance
		try {
			if (request.getParameter("code") != null) {
				LOG.info("Got a code. Attempting to exchange for a google access token.");

				AuthorizationCodeFlow flow = authUtil.newAuthorizationCodeFlow();

				TokenResponse tokenResponse = flow.newTokenRequest(request.getParameter("code"))
						.setRedirectUri(WebUtil.buildUrl(request, "/authorize-google")).execute();

				// Extract the Google User ID from the ID token in the auth
				// response
				String userId = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getUserId();

				LOG.info("Access token is: " + ((GoogleTokenResponse) tokenResponse).getAccessToken());

				LOG.info("Refresh token is: "+ ((GoogleTokenResponse) tokenResponse).getRefreshToken());

				LOG.info("Code exchange worked. User " + userId + " logged in.");

				// Set it into the session
				AuthUtil.setUserId(request, userId);

				flow.createAndStoreCredential(tokenResponse, userId);

				// The dance is done. Do our bootstrapping stuff for this user
				newUserBootstrapper.bootstrapNewUser(request, userId);
				
				HttpSession session = request.getSession();
				
				GoogleOauthSession oauthSession = new GoogleOauthSession();
				
				oauthSession.setAccessToken(((GoogleTokenResponse) tokenResponse).getAccessToken());
				
				oauthSession.setRefreshToken(((GoogleTokenResponse) tokenResponse).getRefreshToken());
				
				Date tday = new Date();
				
				oauthSession.setExpires(tday.getTime() + ((GoogleTokenResponse) tokenResponse).getExpiresInSeconds() * 1000);
				
				oauthSession.setUserId(userId);
				
				session.setAttribute("googleOauthSession", oauthSession);
				
				// add the user's email address to the store
				Entity entity = store.load(userId);
				
				entity.setProperty("email", session.getAttribute("googleEmail"));
				
				store.update(entity);				

				// Redirect back to index
				response.sendRedirect(WebUtil.buildUrl(request, "/"));

				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		AuthorizationCodeFlow flow;
		try {
			
			flow = authUtil.newAuthorizationCodeFlow();
			
			GenericUrl url = flow.newAuthorizationUrl().setRedirectUri(WebUtil.buildUrl(request, "/authorize-google"));
			
			url.set("approval_prompt", "force");
			
			response.sendRedirect(url.build());
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "index";
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

	public AuthUtil getAuthUtil() {
		return authUtil;
	}

	@Autowired(required=true)
	public void setAuthUtil(AuthUtil authUtil) {
		this.authUtil = authUtil;
	}

	public NewUserBootstrapper getNewUserBootstrapper() {
		return newUserBootstrapper;
	}

	@Autowired(required=true)
	public void setNewUserBootstrapper(NewUserBootstrapper newUserBootstrapper) {
		this.newUserBootstrapper = newUserBootstrapper;
	}
	
	public GlassRedditCredentialStore getStore() {
		return store;
	}

	@Autowired(required=true)
	public void setStore(GlassRedditCredentialStore store) {
		this.store = store;
	}	
	
}
