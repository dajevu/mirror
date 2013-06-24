package com.zazarie.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
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
import com.google.glassware.MirrorClient;
import com.zazarie.domain.reddit.Me;
import com.zazarie.domain.reddit.NewFeed;
import com.zazarie.domain.reddit.RedditOauth;

@Component
public class RedditAPIHelper {

	private static final Logger LOG = Logger.getLogger(RedditAPIHelper.class.getSimpleName());
	
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
	
	public static NewFeed getArticlesBySubreddit(String subreddit) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

		URL url = new URL("http://www.reddit.com/r/aww/new.json");	
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		
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
	
	public static NewFeed getNewArticles(String accessToken) throws Exception {
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

		URL url = new URL("https://oauth.reddit.com/new");	
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "application/json");
		conn.addRequestProperty("Authorization", "Bearer " + accessToken);
		
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


}
