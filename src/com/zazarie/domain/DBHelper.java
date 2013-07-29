package com.zazarie.domain;

import java.util.Date;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@Component
public class DBHelper {
	
	private static final Logger LOG = Logger.getLogger(DBHelper.class.getSimpleName());
	
	private static String ENTITY_TYPE = "RedditArticle";
	private static String USER_ENTITY_TYPE = "com.zazarie.domain.GlassRedditCredentialStore"; // name is assigned in that class if you want to change
	
	// database columns
	public static String DB_USER_ID = "userId";
	public static String DB_TIMELINE_ID = "timelineId";
	public static String DB_REDDIT_ID = "redditId";
	public static String DB_SEND_TO_GLASS_DATE = "sentToGlassDate";
	public static String DB_ARTICLE_URL = "articleUrl";
	public static String DB_ARTICLE_TITLE = "articleTitle";
	
	public static Entity addArticle(String userId, String redditArticleId, String articleUrl, String title) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Key key = KeyFactory.createKey(ENTITY_TYPE, userId + "-" + redditArticleId);
		
		// check to determine if record already exists, if so, ignore
		
		Entity exists;
		try {
			exists = datastore.get(key);
			LOG.info("Article already found! " + key.getName());
			return exists;
		} catch (EntityNotFoundException e) {
			LOG.info("Article to be added " + key.getName())			 
			;
		}
		
		Entity article = new Entity(key);
		
		article.setProperty(DB_USER_ID, userId);
		article.setProperty(DB_TIMELINE_ID, null);
		article.setProperty(DB_REDDIT_ID, redditArticleId);
		article.setProperty(DB_SEND_TO_GLASS_DATE, null);
		article.setProperty(DB_ARTICLE_URL, articleUrl);
		article.setProperty(DB_ARTICLE_TITLE, title);
		
		datastore.put(article);
		
		return article;
	}
	
	public static Entity loadArticle(String key) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		try {
			return datastore.get(KeyFactory.stringToKey(key));
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Entity getArticle(String userId, String timelineId) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userFilter = new FilterPredicate(DB_USER_ID,FilterOperator.EQUAL, userId);
		Filter timelineFilter = new FilterPredicate(DB_TIMELINE_ID,FilterOperator.EQUAL, timelineId);
		
		Filter compositeFilter = CompositeFilterOperator.and(userFilter, timelineFilter);
		
		Query q = new Query(ENTITY_TYPE).setFilter(compositeFilter);
		
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable()) {
		  return result;
		}
		
		return null;
	}
	
	public static void updateArticle(Entity entity) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		datastore.put(entity);
	}
	
	public static Iterable<Entity> returnAllUsers() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q = new Query(USER_ENTITY_TYPE);
		
		PreparedQuery pq = datastore.prepare(q);
		
		return pq.asIterable();
		
	}
	
	public static Iterable<Entity> returnAllArticles() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Filter userFilter = new FilterPredicate(DB_SEND_TO_GLASS_DATE,FilterOperator.EQUAL, null);
		
		Query q = new Query(ENTITY_TYPE).setFilter(userFilter);
		
		PreparedQuery pq = datastore.prepare(q);
		
		return pq.asIterable();
		
	}
}
