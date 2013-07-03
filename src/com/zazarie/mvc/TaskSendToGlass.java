package com.zazarie.mvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.appengine.api.datastore.Entity;
import com.google.glassware.MirrorClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zazarie.domain.DBHelper;
import com.zazarie.domain.GlassRedditCredentialStore;
import com.zazarie.service.MirrorAPIHelper;
import com.zazarie.service.RedditAPIHelper;
import com.zazarie.service.WebUtil;

@Controller
public class TaskSendToGlass {

	private static final Logger LOG = Logger.getLogger(TaskSendToGlass.class.getSimpleName());
	
	private MirrorAPIHelper mirrorAPIHelper;
	
	private GlassRedditCredentialStore store;
	
	@RequestMapping(value = "/taskSendToGlass", method = RequestMethod.GET)
	@ResponseBody
	public String sendToGlass(ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		LOG.info("Inside sendToGlass");
		
		Iterable<Entity> articles = DBHelper.returnAllArticles();
		
		Credential credential = null;
		
		for (Entity article : articles) {
			LOG.info("Processing article " + article.getKey().getName());
			
			Entity userEntity = store.load((String) article.getProperty("userId"));
			
			credential = MirrorAPIHelper.getCredentialFromToken((String) userEntity.getProperty("accessToken"));
			
	    	// Let's make sure their oauth session is valid
	    	userEntity = mirrorAPIHelper.checkOauthToken((String) article.getProperty("userId"), request);
			
			TimelineItem timelineItem = new TimelineItem();

			timelineItem.setText((String) article.getProperty(DBHelper.DB_ARTICLE_TITLE));

			timelineItem.setSourceItemId((String) article.getProperty(DBHelper.DB_REDDIT_ID));

			// sends the notification sound to alert user when item arrives
			timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

			List<MenuItem> menuItemList = new ArrayList<MenuItem>();
			// Built in actions
			//menuItemList.add(new MenuItem().setAction("REPLY"));
			menuItemList.add(new MenuItem().setAction("READ_ALOUD"));

			// And custom actions
			List<MenuValue> menuValues = new ArrayList<MenuValue>();
			
			menuValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(request, "/static/images/drill.png")).setDisplayName("UpVote"));
			
			menuItemList.add(new MenuItem().setValues(menuValues).setId("upVote").setAction("CUSTOM"));

			timelineItem.setMenuItems(menuItemList);

			try {

				URL imageUrl = new URL((String) article.getProperty(DBHelper.DB_THUMBNAIL_URL));
				
				BufferedReader in = new BufferedReader(new InputStreamReader(imageUrl.openStream()));

				String timelineId = MirrorClient.insertTimelineItem(credential, timelineItem, "image/jpeg", MirrorAPIHelper.getImageBytesFromURL(imageUrl));
				
				article.setProperty(DBHelper.DB_SEND_TO_GLASS_DATE, new Date());
				
				article.setProperty(DBHelper.DB_TIMELINE_ID, timelineId);
				
				LOG.info("Updating article..." + article.getKey().getName() + " timeline id: " + article.getProperty(DBHelper.DB_TIMELINE_ID));
				
				DBHelper.updateArticle(article);
				

			} catch (IOException e) {
				LOG.warning("IOERR:" + article.getProperty(DBHelper.DB_REDDIT_ID) + " \n" + e);
				e.printStackTrace();
			} catch (Exception e) {
				LOG.warning("ERR:" + article.getProperty(DBHelper.DB_REDDIT_ID) + " \n" + e);
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public MirrorAPIHelper getMirrorAPIHelper() {
		return mirrorAPIHelper;
	}

	@Autowired(required=true)
	public void setMirrorAPIHelper(MirrorAPIHelper mirrorAPIHelper) {
		this.mirrorAPIHelper = mirrorAPIHelper;
	}
	
	public GlassRedditCredentialStore getStore() {
		return store;
	}

	@Autowired(required=true)
	public void setStore(GlassRedditCredentialStore store) {
		this.store = store;
	}

}
