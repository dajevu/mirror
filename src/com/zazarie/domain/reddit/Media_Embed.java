package com.zazarie.domain.reddit;

import java.io.Serializable;

public class Media_Embed  implements Serializable  {

	String content;
	int width;
	boolean scrolling;
	int height;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isScrolling() {
		return scrolling;
	}

	public void setScrolling(boolean scrolling) {
		this.scrolling = scrolling;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "Media_Embed [content=" + content + ", width=" + width
				+ ", scrolling=" + scrolling + ", height=" + height + "]";
	}
	
	
}
