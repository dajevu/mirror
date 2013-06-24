package com.zazarie.domain.reddit;

import java.io.Serializable;
import java.util.Date;

public class Me  implements Serializable  {
	private String name;
	private long created;
	private long created_utc;
	private String link_karma;
	private String comment_karma;
	private boolean over_18;
	private boolean is_gold;
	private boolean has_verified_email;
	private String id;
	private boolean is_mod;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getLink_karma() {
		return link_karma;
	}
	public void setLink_karma(String link_karma) {
		this.link_karma = link_karma;
	}
	public String getComment_karma() {
		return comment_karma;
	}
	public void setComment_karma(String comment_karma) {
		this.comment_karma = comment_karma;
	}
	public boolean isOver_18() {
		return over_18;
	}
	public void setOver_18(boolean over_18) {
		this.over_18 = over_18;
	}
	public boolean isIs_gold() {
		return is_gold;
	}
	public void setIs_gold(boolean is_gold) {
		this.is_gold = is_gold;
	}
	public boolean isHas_verified_email() {
		return has_verified_email;
	}
	public void setHas_verified_email(boolean has_verified_email) {
		this.has_verified_email = has_verified_email;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getCreated() {
		return created;
	}
	public void setCreated(long created) {
		this.created = created;
	}
	public long getCreated_utc() {
		return created_utc;
	}
	public void setCreated_utc(long created_utc) {
		this.created_utc = created_utc;
	}
	public boolean isIs_mod() {
		return is_mod;
	}
	public void setIs_mod(boolean is_mod) {
		this.is_mod = is_mod;
	}
	
	@Override
	public String toString() {
		return "Me [name=" + name + ", created=" + created + ", created_utc="
				+ created_utc + ", link_karma=" + link_karma
				+ ", comment_karma=" + comment_karma + ", over_18=" + over_18
				+ ", is_gold=" + is_gold + ", has_verified_email="
				+ has_verified_email + ", id=" + id + ", is_mod=" + is_mod
				+ "]";
	}	
	
}

/* 
{
"name": "dajevu",
"created": 1322582418,
"created_utc": 1322582418,
"link_karma": 6,
"comment_karma": 0,
"over_18": false,
"is_gold": false,
"is_mod": false,
"has_verified_email": false,
"id": "6c45z"
}
*/