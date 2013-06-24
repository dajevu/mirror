package com.zazarie.domain.reddit;

import java.io.Serializable;

public class Children implements Serializable {
	
	String kind;
	Data data;
	
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public Data getData() {
		return data;
	}
	public void setData(Data data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "Children [kind=" + kind + ", data=" + data + "]";
	}
	
	
}
