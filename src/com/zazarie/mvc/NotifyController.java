package com.zazarie.mvc;

import java.io.BufferedReader;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.client.auth.oauth2.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.Mirror.Timeline;
import com.google.api.services.mirror.Mirror.Timeline.Get;
import com.google.api.services.mirror.model.Notification;
import com.google.api.services.mirror.model.TimelineItem;
import com.zazarie.domain.GlassRedditCredentialStore;
import com.zazarie.service.MirrorAPIHelper;
import com.zazarie.service.RedditAPIHelper;

@Controller
public class NotifyController {

	private static final Logger LOG = Logger.getLogger(NotifyController.class.getSimpleName());
	
	private AuthUtil authUtil;
	
	private GlassRedditCredentialStore store;

	@RequestMapping(value = "/notify", method = RequestMethod.POST)
	@ResponseBody
	public String notify(ModelMap model, HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
		
		LOG.info("Landed inside /notify servlet...");
		
	    response.setContentType("text/html");
	    Writer writer = response.getWriter();
	    writer.append("OK");
	    writer.close();
	    
	    // Get the notification object from the request body (into a string so we
	    // can log it)
	    BufferedReader notificationReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
	    String notificationString = "";

	    // Count the lines as a very basic way to prevent Denial of Service attacks
	    int lines = 0;
	    while (notificationReader.ready()) {
	      notificationString += notificationReader.readLine();
	      lines++;

	      // No notification would ever be this long. Something is very wrong.
	      if(lines > 1000) {
	        throw new IOException("Attempted to parse notification payload that was unexpectedly long.");
	      }
	    }

	    LOG.info("got raw notification " + notificationString);

	    JsonFactory jsonFactory = new JacksonFactory();

	    // If logging the payload is not as important, use
	    // jacksonFactory.fromInputStream instead.
	    Notification notification = jsonFactory.fromString(notificationString, Notification.class);

	    LOG.info("Got a notification with ID: " + notification.getItemId());

	    // Figure out the impacted user and get their credentials for API calls
	    String userId = notification.getUserToken();
	    
	    LOG.info("userId from notification is: " + userId);
	    	    
	    LOG.info("Notification impacted timeline item with ID: " + notification.getItemId());
	    
	    Credential credential = authUtil.getCredential(userId);
	    
	    Mirror mirror = MirrorAPIHelper.getMirror(credential);
	    
	    TimelineItem timelineItem = mirror.timeline().get(notification.getItemId()).execute();
	    
	    // NOTE: Thie getInReplyTo() is really the magic here - it returns ID form timeline item that was originally sent. Correlation id.
	    
	    LOG.info("Timeline reddit item replied to was: " +  timelineItem.getText() + " reply to is: " + timelineItem.getInReplyTo() + " source id is: " + timelineItem.getSourceItemId());
	    
	    Entity oauthCred = store.load(userId);
	    
	    try {
			RedditAPIHelper.vote((String) oauthCred.getProperty("redditAccessToken"), timelineItem.getSourceItemId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    // this doesn't appear to work - wanted to remove the item
	    mirror.timeline().delete(notification.getItemId());
	    
		return null;
		
	}

	public AuthUtil getAuthUtil() {
		return authUtil;
	}

	@Autowired(required=true)
	public void setAuthUtil(AuthUtil authUtil) {
		this.authUtil = authUtil;
	}
	
	public GlassRedditCredentialStore getStore() {
		return store;
	}

	@Autowired(required=true)
	public void setStore(GlassRedditCredentialStore store) {
		this.store = store;
	}	
}
