package com.zazarie.mvc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.glassware.MirrorClient;
import com.zazarie.domain.GoogleOauthSession;
import com.zazarie.domain.reddit.Children;
import com.zazarie.domain.reddit.NewFeed;

@Controller
public class SendController {
	
	private static final Logger LOG = Logger.getLogger(SendController.class.getSimpleName());

	@RequestMapping(method = RequestMethod.POST, value = "/sendArticle")
	@ResponseBody
	public final String sendArticle(@RequestBody String articleId, HttpServletRequest request, HttpServletResponse response) {
		
		articleId = articleId.substring(0,articleId.length()-1);
		
		LOG.info("Sending article: " + articleId + " to Glass");
		
		NewFeed feed = (NewFeed) request.getSession().getAttribute("redditArticles");
		
		for ( Children ind : feed.getData().getChildren() ) {
			
			LOG.info("Scrolling through: " + ind.getData().getId());
			
			if (ind.getData().getId().equals(articleId)) {
				
				LOG.info("Match found!");
				
				// TODO : Split into a reusable function
				HttpTransport httpTransport = new UrlFetchTransport();
				JsonFactory jsonFactory = new JacksonFactory();
				
				//Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod());
				
				Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
			    	.setJsonFactory(jsonFactory)
			    	.setTransport(httpTransport)
			    	.build();
				
				GoogleOauthSession googleOauthSession = (GoogleOauthSession) request.getSession().getAttribute("googleOauthSession");
				
				credential.setAccessToken(googleOauthSession.getAccessToken());
				
				LOG.info("Using access token: " + credential.getAccessToken());
				
				TimelineItem timelineItem = new TimelineItem();
				
				timelineItem.setText(ind.getData().getTitle());
				
				// sends the notification sound to alert user when item arrives
				timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));
				
				List<MenuItem> menuItemList = new ArrayList<MenuItem>();
			    // Built in actions
			    menuItemList.add(new MenuItem().setAction("REPLY"));
			    menuItemList.add(new MenuItem().setAction("READ_ALOUD"));

			    timelineItem.setMenuItems(menuItemList);
				
				try {
					
					URL imageUrl = new URL(ind.getData().getUrl());
			        BufferedReader in = new BufferedReader(new InputStreamReader(imageUrl.openStream()));
			        
					MirrorClient.insertTimelineItem(credential, timelineItem, "image/jpeg", getImageBytesFromURL(imageUrl));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
				break;
			}
			
		}

		return "Complete";
	}
	
	public byte[] getImageBytesFromURL(URL url) throws Exception {
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
}
