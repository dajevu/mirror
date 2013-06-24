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
import com.zazarie.service.RedditAPIHelper;
import com.zazarie.service.WebUtil;

@Controller
public class MainController {

	private static final Logger LOG = Logger.getLogger(MainController.class.getSimpleName());
	
	private GlassRedditCredentialStore store;
	
	private AuthUtil authUtil;
	
	private @Value("${reddit_client_id}") String redditClientId;

	private @Value("${reddit_client_secret}") String redditPass;
	
	private @Value("${reddit_client_token}") String redditToken;
	
	private @Value("${reddit_client_authorize}") String redditAuthorize;
	
	private @Value("${reddit_client_scope}") String redditScope;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(ModelMap model, HttpServletRequest request, HttpServletResponse response) {

		LOG.setLevel(Level.FINEST);
		
		model.addAttribute("googleEmail", request.getSession().getAttribute("googleEmail"));
		
		checkGoogleOauth(model, request, response);
		
		checkRedditOauth(model, request, response);

		model.addAttribute("googleLoggedIn", request.getAttribute("googleLoggedin"));
		
		model.addAttribute("googleLoginRedirect", request.getAttribute("googleLoginRedirect"));
		
		model.addAttribute("googleLogoutRedirect", request.getAttribute("googleLogoutRedirect"));
		
		/* test code that illustrates how to insert into timeline

		HttpTransport httpTransport = new UrlFetchTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		
		//Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
		
		Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
	    	.setJsonFactory(jsonFactory)
	    	.setTransport(httpTransport)
	    	.setTokenServerEncodedUrl("http://localhost:1091")
	    	.build();
		
		credential.setAccessToken("ya29.AHES6ZRFdy1kHiUN0FvWgzFQWiukvTSIPMBfT_BE34XJCZfj-dW6fPr1");
		
		TimelineItem timelineItem = new TimelineItem();
		
		timelineItem.setText("Test");
		
		try {
			MirrorClient.insertTimelineItem(credential, timelineItem);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		if (model.get("redditOauthCredentialed") != null) {
			// if oauth is completed for reddit, display some articles
			if ((Boolean) model.get("redditOauthCredentialed")) {
				
				NewFeed feed = null;
				try {
					//feed = RedditAPIHelper.getNewArticles( ((RedditOauthSession) request.getSession().getAttribute("redditOauthSession")).getAccessToken());
					feed = RedditAPIHelper.getArticlesBySubreddit("aww");
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
		
			// check to see whether a user entry already exists
			Entity entity = store.findByGoogleEmail((String) request.getSession().getAttribute("googleEmail"));
			
			if (entity != null) {
				
				LOG.info("Matching entity found for user in store");
				
				// check google's oauth
				if ( (entity.getProperty("redditAccessToken") != null) && entity.getProperty("redditRefreshToken") != null) {
					
					// populate the session object with the values from the datastore
					RedditOauthSession oauthSession = new RedditOauthSession();
					
					// check if access token has expired, if so, refresh it
					long expires = (Long) entity.getProperty("redditExpires");
					
					if (new Date().getTime() >= expires) {
						
						LOG.info("Refreshing expired Reddit access token");
						
						String basicAuth = RedditAPIHelper.buildAuthorizationHeader(redditClientId, redditPass);
						
						// refresh the access token
						
						String body = RedditAPIHelper.buildOauthBodyRefresh(WebUtil.buildUrl(request,"/authorize-reddit"), 
								(String) entity.getProperty("redditRefreshToken"), redditClientId, redditPass, redditScope);
						
						RedditOauth oauth = null;
						
						try {
							oauth = RedditAPIHelper.fetchAccessToken(basicAuth, body, redditToken);
							
							LOG.info("oauth is: " + oauth);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						// save the Reddit oauth credentials
						entity.setProperty("redditAccessToken",  oauth.getAccess_token());
						
						entity.setProperty("redditRefreshToken", oauth.getRefresh_token());
						
						Date today = new Date();
						
						entity.setProperty("redditExpires", today.getTime() + (oauth.getExpires_in() * 1000));
						
						store.update(entity);
					}
					
					oauthSession.setAccessToken((String) entity.getProperty("redditAccessToken"));
					
					oauthSession.setRefreshToken((String) entity.getProperty("redditRefreshToken"));
					
					oauthSession.setUserId(entity.getKey().getName());
					
					oauthSession.setExpires((Long) entity.getProperty("redditExpires"));
					
					request.getSession().setAttribute("redditOauthSession", oauthSession);
					
					model.addAttribute("redditOauthCredentialed", true);
				}
			} else // no matching oauth credentails found for user
				model.addAttribute("redditOauthCredentialed", false);
		} else {
			// googleOauthSession already initialized/populated
			model.addAttribute("redditOauthCredentialed", true);
		}
	}
	
	private void checkGoogleOauth(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		
		// user hasn't gone through google's oauth in this session
		if (request.getSession().getAttribute("googleOauthSession") == null) {
		
			// check to see whether a user entry already exists
			Entity entity = store.findByGoogleEmail((String) request.getSession().getAttribute("googleEmail"));
			
			if (entity != null) {
				
				LOG.info("Matching entity found for user in store");
				
				// check google's oauth
				if ( (entity.getProperty("accessToken") != null) && entity.getProperty("refreshToken") != null) {
					
					// populate the session object with the values from the datastore
					GoogleOauthSession oauthSession = new GoogleOauthSession();
					
					// check if access token has expired, if so, refresh it
					long expires = (Long) entity.getProperty("expirationTimeMillis");
					
					if (new Date().getTime() >= expires) {
						
						// refresh the access token
						try {
							LOG.info("Refreshing access token for Google, authUtil is: " + oauthSession.getUserId());
							
							authUtil.getCredential(oauthSession.getUserId()).refreshToken();
							
							// unfortunately, after we refresh the token, the entry is removed/re-added by google, so we need to re-populate the entity with the other attributes
							
							Entity newEntity = store.load(oauthSession.getUserId());
							
							newEntity.setProperty("email", entity.getProperty("email"));
							newEntity.setProperty("redditAccessToken", entity.getProperty("redditAccessToken"));
							newEntity.setProperty("redditRefreshToken", entity.getProperty("redditRefreshToken"));
							newEntity.setProperty("redditExpires", entity.getProperty("redditExpires"));
							
							store.update(newEntity);
							
							oauthSession.setAccessToken((String) entity.getProperty("accessToken"));
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						LOG.info("No need to refresh access token");
					}
					
					oauthSession.setAccessToken((String) entity.getProperty("accessToken"));
					
					oauthSession.setRefreshToken((String) entity.getProperty("refreshToken"));
					
					oauthSession.setUserId(entity.getKey().getName());
					
					oauthSession.setExpires((Long) entity.getProperty("expirationTimeMillis"));
					
					request.getSession().setAttribute("googleOauthSession", oauthSession);
					
					model.addAttribute("googleOauthCredentialed", true);
				}
			} else {// no matching oauth credentails found for user 
				model.addAttribute("googleOauthCredentialed", false);
			}
		} else {
			// googleOauthSession already initialized/populated
			model.addAttribute("googleOauthCredentialed", true);
		}
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
	
	
}
