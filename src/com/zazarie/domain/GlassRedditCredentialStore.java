package com.zazarie.domain;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

@Component
public class GlassRedditCredentialStore implements CredentialStore {

	private static final String KIND = GlassRedditCredentialStore.class.getName();
	
	private static final Logger LOG = Logger.getLogger(GlassRedditCredentialStore.class.getSimpleName());

	public List<String> listAllUsers() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query userQuery = new Query(KIND);
		Iterable<Entity> userEntities = datastore.prepare(userQuery)
				.asIterable();

		List<String> userIds = new ArrayList<String>();
		for (Entity userEntity : userEntities) {
			userIds.add(userEntity.getKey().getName());
		}
		return userIds;
	}

	@Override
	public void store(String userId, Credential credential) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity entity = new Entity(KIND, userId);
		
		entity.setProperty("accessToken", credential.getAccessToken());
		entity.setProperty("refreshToken", credential.getRefreshToken());
		entity.setProperty("expirationTimeMillis",credential.getExpirationTimeMilliseconds());

		datastore.put(entity);
	}

	@Override
	public void delete(String userId, Credential credential) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Key key = KeyFactory.createKey(KIND, userId);
		
		datastore.delete(key);
	}

	@Override
	public boolean load(String userId, Credential credential) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(KIND, userId);
		
		try {
			Entity entity = datastore.get(key);
			credential.setAccessToken((String) entity.getProperty("accessToken"));
			credential.setRefreshToken((String) entity.getProperty("refreshToken"));
			credential.setExpirationTimeMilliseconds((Long) entity.getProperty("expirationTimeMillis"));
			return true;
		} catch (EntityNotFoundException exception) {
			return false;
		}
	}
	
	public Entity load(String userId) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(KIND, userId);
		
		try {
			Entity entity = datastore.get(key);

			return entity;
			
		} catch (EntityNotFoundException exception) {
			return null;
		}
	}
	
	public void update(Entity entity) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		datastore.put(entity);
	}
	
	public Entity findByGoogleEmail(String email) {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter emailFilter = new FilterPredicate("email",FilterOperator.EQUAL, email);
		
		// Use class Query to assemble a query
		Query q = new Query(KIND).setFilter(emailFilter);

		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable()) {
		  return result;
		}
		
		return null;

	}
}