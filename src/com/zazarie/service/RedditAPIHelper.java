package com.zazarie.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.Credential.AccessMethod;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.appengine.api.datastore.Entity;
import com.google.glassware.MirrorClient;
import com.zazarie.domain.GlassRedditCredentialStore;
import com.zazarie.domain.RedditOauthSession;
import com.zazarie.domain.reddit.Children;
import com.zazarie.domain.reddit.Me;
import com.zazarie.domain.reddit.NewFeed;
import com.zazarie.domain.reddit.RedditOauth;

@Component
public class RedditAPIHelper {

	private static final Logger LOG = Logger.getLogger(RedditAPIHelper.class.getSimpleName());
	
	private @Value("${reddit_client_id}") String redditClientId;

	private @Value("${reddit_client_secret}") String redditPass;
	
	private @Value("${reddit_client_token}") String redditToken;
	
	private @Value("${reddit_client_scope}") String redditScope;
	
	private static String USER_AGENT = "RedditForGlass v1 by /u/odawg2p";
	
	private GlassRedditCredentialStore store;
	
	public static void main(String[] args) throws Exception {
		

		/*
		String basicAuth = buildAuthorizationHeader("bJOpFK2Q09RmuQ", "PK1ZNPtrKj_WWfyEbCmkNXKw6IM");
		
		LOG.info("basicAuth header is: " + basicAuth);
		
		String body = buildOauthBodyRefresh("http://localhost:8080/authorize-reddit", "Vx9TVc8UU9vnCz4ZXBVJIcqBVaQ", "bJOpFK2Q09RmuQ", "PK1ZNPtrKj_WWfyEbCmkNXKw6IM", "identity");
		
		LOG.info("url is: " + body);
		
		try {
			RedditOauth oauth = fetchAccessToken(basicAuth, body, "https://ssl.reddit.com/api/v1/access_token");
			
			LOG.info("oauth is: " + oauth);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

		//getUserInfo("oAV_b2Tn96qj8ItpCOqmWBIdeVc");
		
		/* Set up the HTTP transport and JSON factory */
		FileInputStream fisTargetFile = new FileInputStream(new File("/Users/user/Documents/workspace-idea/mirror-java-starter-demo/RedditSamples/new.json"));

		String targetFileStr = IOUtils.toString(fisTargetFile, "UTF-8");
		
		System.out.println("file is: " + targetFileStr);
		
		ObjectMapper mapper = new ObjectMapper();
		
		NewFeed feed = mapper.readValue(targetFileStr, NewFeed.class);
		
		System.out.println("feed is: "  + feed.getData().getChildren()[0].getData().getTitle());


	}
	
	//http://www.reddit.com/r/aww/new.json
	
	public static NewFeed getArticlesBySubreddit(String subreddit, int limit) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

		URL url = new URL("http://www.reddit.com/r/" + subreddit  + "/hot.json?limit=" + limit);	
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		
		conn.connect();
		
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		
		NewFeed feed = mapper.readValue(br, NewFeed.class);

		conn.disconnect();
		
		LOG.info("NewFeed is: " + feed);
		
		return feed;
	}
	
	public static NewFeed getUserArticles(int limit, String accessToken) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		String listOfSubreddits = "";
		
		URL subreddits = new URL("https://oauth.reddit.com/reddits/mine/subscriber.json?limit=100");	
		
		HttpURLConnection conn = (HttpURLConnection) subreddits.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.addRequestProperty("Authorization", "Bearer " + accessToken);
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.connect();
		
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		
		NewFeed feed = mapper.readValue(br, NewFeed.class);
		
		while(feed != null){
			String tmp;
			for (Children feeds : feed.getData().getChildren()) {
				listOfSubreddits += (feeds.getData().getDisplay_name()) + "+";
			}
		}

		conn.disconnect();
		
		return getArticlesBySubreddit(listOfSubreddits, limit);
	}
	
	
	public static NewFeed getNewArticles(String accessToken) throws Exception {
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

		URL url = new URL("https://oauth.reddit.com/new");	
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.addRequestProperty("Authorization", "Bearer " + accessToken);
		conn.setRequestProperty("User-Agent", USER_AGENT);
		
		conn.connect();
		
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		
		NewFeed feed = mapper.readValue(br, NewFeed.class);

		conn.disconnect();
		
		LOG.info("NewFeed is: " + feed);
		
		return feed;
	}
	
	public static Me getUserInfo(String accessToken) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

		URL url = new URL("https://oauth.reddit.com/api/v1/me");	
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.addRequestProperty("Authorization", "Bearer " + accessToken);
		conn.setRequestProperty("User-Agent", USER_AGENT);

		conn.connect();
		
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		
		Me me = mapper.readValue(br, Me.class);

		conn.disconnect();
		
		LOG.info("Me is: " + me);
		
		return me;
	}
	
	/*
	 * Builds the Basic authentication header required by Reddit
	 */
	public static String buildAuthorizationHeader (String username, String password) {
		
		return Base64.encodeBase64String((username + ":" + password).getBytes());
		
	}
	
	/*
	 * Builds the HTTP OAuth body content for Reddit
	 */
	public static String buildOauthBody(String redirectUrl, String accessCode) {
		
		URLCodec encoder = new URLCodec();
		
		try {
			// valid grant_types are refresh_token and authorization_code
			
			return "grant_type=authorization_code&redirect_uri=" + encoder.encode(redirectUrl) + "&code=" + accessCode;
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/*
	 * Builds the HTTP OAuth body content for Reddit
	 */
	public static String buildOauthBodyRefresh(String redirectUrl, String refreshToken, String clientId, String clientSecret, String scope) {
		
		URLCodec encoder = new URLCodec();
		
		try {
			// valid grant_types are refresh_token and authorization_code
			
			return "grant_type=refresh_token&redirect_uri=" + encoder.encode(redirectUrl) + "&refresh_token=" + refreshToken 
					+ "&duration=permanent&scope=" + scope+"&client_id="+clientId+"&client_secret="+clientSecret+"&state=nnvul";
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static RedditOauth fetchAccessToken(String basicAuthCredentials, String body, String endpointUrl) throws Exception {
		
		// TODO : Remove LOG.info statements and replace w/Log

		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

		URL url = new URL(endpointUrl);
		
		LOG.info("Endpoint URL is: " + endpointUrl);
		LOG.info("Authorization is: " + basicAuthCredentials);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.addRequestProperty("Authorization", "Basic " + basicAuthCredentials);

		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
		writer.write(body);
		writer.close();

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		String output;
		LOG.info("Output from Server .... \n");
		
		RedditOauth rOauth = mapper.readValue(br, RedditOauth.class);

		conn.disconnect();
		
		LOG.info("Reddit json obj is: " + rOauth);
		
		return rOauth;
	}

	public static void vote(String accessToken, String itemId) throws Exception {
		
		URL url = new URL("https://oauth.reddit.com/api/vote");
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.addRequestProperty("Authorization", "Bearer " + accessToken);
		conn.setRequestProperty("User-Agent", USER_AGENT);

		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
		writer.write("dir=1&id=t3_" + itemId);
		writer.close();

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}

		conn.disconnect();
		
	}
	
	public Entity checkOauthToken(String emailAddr, HttpServletRequest request) {
		
		// check to see whether a user entry already exists
		Entity entity = store.findByGoogleEmail(emailAddr);

		if (entity != null) {

			LOG.info("Matching entity found for user in store");
			
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
				
				return entity;
			}

			return entity;
		}
		
		return null;
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
	
	public GlassRedditCredentialStore getStore() {
		return store;
	}

	@Autowired(required=true)
	public void setStore(GlassRedditCredentialStore store) {
		this.store = store;
	}

	public String getRedditScope() {
		return redditScope;
	}

	public void setRedditScope(String redditScope) {
		this.redditScope = redditScope;
	}


}
