package com.zazarie.domain.reddit;

import java.io.Serializable;

public class NewFeed  implements Serializable {

	String kind;
	Data data;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	@Override
	public String toString() {
		return "NewFeed [kind=" + kind + ", data=" + data + "]";
	}
	
	
}
