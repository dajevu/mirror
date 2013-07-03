package com.zazarie.mvc;

import java.io.BufferedReader;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;

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
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.glassware.MirrorClient;
import com.zazarie.domain.DBHelper;
import com.zazarie.domain.GoogleOauthSession;
import com.zazarie.domain.reddit.Children;
import com.zazarie.domain.reddit.NewFeed;
import com.zazarie.service.MirrorAPIHelper;
import com.zazarie.service.WebUtil;

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

				Credential credential = MirrorAPIHelper.getCredential((GoogleOauthSession) request.getSession().getAttribute("googleOauthSession"));

				LOG.info("Using access token: " + credential.getAccessToken());

				TimelineItem timelineItem = new TimelineItem();

				timelineItem.setText(ind.getData().getTitle());

				timelineItem.setSourceItemId(ind.getData().getId());

				// sends the notification sound to alert user when item arrives
				timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

				List<MenuItem> menuItemList = new ArrayList<MenuItem>();
				// Built in actions
				//menuItemList.add(new MenuItem().setAction("REPLY"));
				//menuItemList.add(new MenuItem().setAction("READ_ALOUD"));

				// And custom actions
				List<MenuValue> menuValues = new ArrayList<MenuValue>();
				
				menuValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(request, "/static/images/drill.png")).setDisplayName("UpVote"));
				
				menuItemList.add(new MenuItem().setValues(menuValues).setId("drill").setAction("CUSTOM"));

				timelineItem.setMenuItems(menuItemList);

				try {

					URL imageUrl = new URL(ind.getData().getUrl());
					BufferedReader in = new BufferedReader(new InputStreamReader(imageUrl.openStream()));

					String timelineId = MirrorClient.insertTimelineItem(credential, timelineItem, "image/jpeg", getImageBytesFromURL(imageUrl));
					
					// Add the article to the database
					//Entity article = DBHelper.addArticle(((GoogleOauthSession) request.getSession().getAttribute("googleOauthSession")).getUserId(), timelineId, ind.getData().getId());
					

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
