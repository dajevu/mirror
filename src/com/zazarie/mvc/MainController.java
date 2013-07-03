package com.zazarie.mvc;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.glassware.MirrorClient;
import com.zazarie.domain.GlassRedditCredentialStore;
import com.zazarie.domain.GoogleOauthSession;
import com.zazarie.domain.RedditOauthSession;
import com.zazarie.domain.reddit.NewFeed;
import com.zazarie.domain.reddit.RedditOauth;
import com.zazarie.service.MirrorAPIHelper;
import com.zazarie.service.RedditAPIHelper;
import com.zazarie.service.WebUtil;

@Controller
public class MainController {

	private static final Logger LOG = Logger.getLogger(MainController.class.getSimpleName());
	
	private GlassRedditCredentialStore store;
	
	private MirrorAPIHelper mirrorAPIHelper;
	
	private RedditAPIHelper redditAPIHelper;
	
	private AuthUtil authUtil;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(ModelMap model, HttpServletRequest request, HttpServletResponse response) {

		LOG.setLevel(Level.FINEST);
		
		model.addAttribute("googleEmail", request.getSession().getAttribute("googleEmail"));
		
		checkGoogleOauth(model, request, response);
		
		checkRedditOauth(model, request, response);

		model.addAttribute("googleLoggedIn", request.getAttribute("googleLoggedin"));
		
		model.addAttribute("googleLoginRedirect", request.getAttribute("googleLoginRedirect"));
		
		model.addAttribute("googleLogoutRedirect", request.getAttribute("googleLogoutRedirect"));
		
		if (model.get("redditOauthCredentialed") != null) {
			// if oauth is completed for reddit, display some articles
			if ((Boolean) model.get("redditOauthCredentialed")) {
				
				NewFeed feed = null;
				try {
					//feed = RedditAPIHelper.getNewArticles( ((RedditOauthSession) request.getSession().getAttribute("redditOauthSession")).getAccessToken());
					feed = RedditAPIHelper.getArticlesBySubreddit("aww", 10);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				request.getSession().setAttribute("redditArticles", feed);
				model.addAttribute("redditArticles", feed);
			}
		}
		return "index";
	}
	
	private void checkRedditOauth(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		
		// user hasn't gone through google's oauth in this session
		if (request.getSession().getAttribute("redditOauthSession") == null) {
			model.addAttribute("redditOauthCredentialed", false);
			return;
		} 

		// check to see whether a user entry already exists
		Entity entity = store.findByGoogleEmail((String) request.getSession().getAttribute("googleEmail"));

		if (entity != null) {

			LOG.info("Matching entity found for user in store");

			// check google's oauth
			if ( (entity.getProperty("redditAccessToken") != null) && entity.getProperty("redditRefreshToken") != null) {

				// populate the session object with the values from the datastore
				RedditOauthSession oauthSession = new RedditOauthSession();

				// this will refresh the access token, if expired
				entity = redditAPIHelper.checkOauthToken((String) request.getSession().getAttribute("googleEmail"), request);

				oauthSession.setAccessToken((String) entity.getProperty("redditAccessToken"));

				oauthSession.setRefreshToken((String) entity.getProperty("redditRefreshToken"));

				oauthSession.setUserId(entity.getKey().getName());

				oauthSession.setExpires((Long) entity.getProperty("redditExpires"));

				request.getSession().setAttribute("redditOauthSession", oauthSession);

				model.addAttribute("redditOauthCredentialed", true);
			} else
				model.addAttribute("redditOauthCredentialed", false);	
				
		} else // no matching oauth credentails found for user
			model.addAttribute("redditOauthCredentialed", false);
	}
	
	private void checkGoogleOauth(ModelMap model, HttpServletRequest request, HttpServletResponse response) {

		// user hasn't gone through google's oauth in this session
		if (request.getSession().getAttribute("googleOauthSession") == null) {

			model.addAttribute("googleOauthCredentialed", false);	
			return;
		}

		// check to see whether a user entry already exists
		Entity entity = store.findByGoogleEmail((String) request.getSession().getAttribute("googleEmail"));

		if (entity != null) {

			LOG.info("Matching entity found for user in store");

			// check google's oauth
			if ( (entity.getProperty("accessToken") != null) && entity.getProperty("refreshToken") != null) {

				// populate the session object with the values from the datastore
				GoogleOauthSession oauthSession = new GoogleOauthSession();

				entity = mirrorAPIHelper.checkOauthToken( ((GoogleOauthSession) request.getSession().getAttribute("googleOauthSession")).getUserId(), request);

				oauthSession.setAccessToken((String) entity.getProperty("accessToken"));

				oauthSession.setRefreshToken((String) entity.getProperty("refreshToken"));

				oauthSession.setUserId(entity.getKey().getName());

				oauthSession.setExpires((Long) entity.getProperty("expirationTimeMillis"));

				request.getSession().setAttribute("googleOauthSession", oauthSession);

				model.addAttribute("googleOauthCredentialed", true);
				
			} else
				model.addAttribute("googleOauthCredentialed", false);	
			
		} else  // no matching oauth credentails found for user 
			model.addAttribute("googleOauthCredentialed", false);

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
	
	public RedditAPIHelper getRedditAPIHelper() {
		return redditAPIHelper;
	}

	@Autowired(required=true)
	public void setRedditAPIHelper(RedditAPIHelper redditAPIHelper) {
		this.redditAPIHelper = redditAPIHelper;
	}

	public MirrorAPIHelper getMirrorAPIHelper() {
		return mirrorAPIHelper;
	}

	@Autowired(required=true)
	public void setMirrorAPIHelper(MirrorAPIHelper mirrorAPIHelper) {
		this.mirrorAPIHelper = mirrorAPIHelper;
	}

	
}
