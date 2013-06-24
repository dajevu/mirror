package com.zazarie.domain;

public interface OauthSession {

	public void setUserId(String userid);
	public String getUserId();
	
	public void setAccessToken(String accessToken);
	public String getAccessToken();
	
	public void setRefreshToken(String accessToken);
	public String getRefreshToken();	
	
	public void setExpires(long exp);
	public long getExpires();
}
