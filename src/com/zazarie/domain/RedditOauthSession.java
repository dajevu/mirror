package com.zazarie.domain;

import java.io.Serializable;

public class RedditOauthSession implements OauthSession, Serializable {

	private static final long serialVersionUID = -4703797660818801138L;
	private String userId;
	private String accessToken;
	private String refreshToken;
	private long expires;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public long getExpires() {
		return expires;
	}
	public void setExpires(long expires) {
		this.expires = expires;
	}

}
