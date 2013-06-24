package com.zazarie.domain.reddit;

import java.io.Serializable;

public class Media  implements Serializable {

	private String type;
	private Oembed oembed;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Oembed getOembed() {
		return oembed;
	}
	public void setOembed(Oembed oembed) {
		this.oembed = oembed;
	}
	@Override
	public String toString() {
		return "Media [type=" + type + ", oembed=" + oembed + "]";
	}
	
	
}
