package com.zazarie.mvc;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.appengine.api.datastore.Entity;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.zazarie.domain.GlassRedditCredentialStore;
import com.zazarie.domain.GoogleOauthSession;
import com.zazarie.domain.RedditOauthSession;
import com.zazarie.domain.reddit.RedditOauth;
import com.zazarie.service.RedditAPIHelper;
import com.zazarie.service.WebUtil;


@Controller
public class RedditOauthController {

	private static final Logger LOG = Logger.getLogger(MainController.class.getSimpleName());

	private @Value("${reddit_client_id}") String redditClientId;

	private @Value("${reddit_client_secret}") String redditPass;
	
	private @Value("${reddit_client_token}") String redditToken;
	
	private @Value("${reddit_client_authorize}") String redditAuthorize;
	
	private @Value("${reddit_client_scope}") String redditScope;
	
	private GlassRedditCredentialStore store;

	@RequestMapping(value = "/authorize-reddit", method = RequestMethod.GET)
	public String authorizeReddit(ModelMap model, HttpServletRequest request, HttpServletResponse response) {

		LOG.info("Inside Reddit authorize...");
		
		HttpSession session = request.getSession();

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
				LOG.info("Got a code. Attempting to exchange for a reddit access token.");

				String code = request.getParameter("code");
				
				String basicAuth = RedditAPIHelper.buildAuthorizationHeader(redditClientId, redditPass);
				
				LOG.info("basicAuth header is: " + basicAuth);
				
				LOG.info("Callback URL is: " + WebUtil.buildUrl(request,"/authorize-reddit"));
				
				String body = RedditAPIHelper.buildOauthBody(WebUtil.buildUrl(request,"/authorize-reddit"), code);
				
				LOG.info("url is: " + body);
				
				RedditOauth oauth = null;
				
				try {
					oauth = RedditAPIHelper.fetchAccessToken(basicAuth, body, redditToken);
					
					LOG.info("oauth is: " + oauth);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Redirect back to index
				response.sendRedirect(WebUtil.buildUrl(request, "/"));
				
				GoogleOauthSession googleOauthsession = (GoogleOauthSession) session.getAttribute("googleOauthSession");
				
				LOG.info("Google userid is: " + googleOauthsession.getUserId());
				
				Entity entity = store.load(googleOauthsession.getUserId());
				
				// save the Reddit oauth credentials
				entity.setProperty("redditAccessToken",  oauth.getAccess_token());
				
				entity.setProperty("redditRefreshToken", oauth.getRefresh_token());
				
				Date today = new Date();
				
				entity.setProperty("redditExpires", today.getTime() + (oauth.getExpires_in() * 1000));
				
				store.update(entity);
				
				RedditOauthSession oauthSession = new RedditOauthSession();
				
				oauthSession.setAccessToken(oauth.getAccess_token());
				
				oauthSession.setRefreshToken(oauth.getRefresh_token());
				
				oauthSession.setExpires((Long) entity.getProperty("redditExpires"));
				
				oauthSession.setUserId("");
				
				session.setAttribute("redditOauthSession", oauthSession);
				
				return null;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			// TODO: Add hardcoded values to property file
			OAuthClientRequest rRequest = OAuthClientRequest
					.authorizationLocation(redditAuthorize).setClientId(redditClientId)
					.setRedirectURI(WebUtil.buildUrl(request,"/authorize-reddit"))
					.setResponseType("code").setScope(redditScope)
					.setParameter("duration", "permanent")
					.setState(UUID.randomUUID().toString()).buildQueryMessage();

			LOG.info("URL for reddit oauth is:: " + rRequest.getLocationUri());

			response.sendRedirect(rRequest.getLocationUri());

		} catch (Exception e) {
			e.printStackTrace();
		}

		model.addAttribute("loggedinReddit", true);

		return "index";
	}

	public String getRedditClientId() {
		return redditClientId;
	}

	public void setRedditClientId(String redditClientId) {
		this.redditClientId = redditClientId;
	}

	public String getRedditPass() {
		return redditPass;
	}

	public void setRedditPass(String redditPass) {
		this.redditPass = redditPass;
	}

	public String getRedditToken() {
		return redditToken;
	}

	public void setRedditToken(String redditToken) {
		this.redditToken = redditToken;
	}

	public String getRedditAuthorize() {
		return redditAuthorize;
	}

	public void setRedditAuthorize(String redditAuthorize) {
		this.redditAuthorize = redditAuthorize;
	}

	public String getRedditScope() {
		return redditScope;
	}

	public void setRedditScope(String redditScope) {
		this.redditScope = redditScope;
	}

	public GlassRedditCredentialStore getStore() {
		return store;
	}

	@Autowired(required=true)
	public void setStore(GlassRedditCredentialStore store) {
		this.store = store;
	}


}
