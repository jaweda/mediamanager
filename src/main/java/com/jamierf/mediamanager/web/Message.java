package com.jamierf.mediamanager.web;

public class Message {

	private final Object value;
	private final String type;

	public Message(Object value) {
		this.value = value;

		type = value.getClass().getSimpleName();
	}

	public Object getValue() {
		return value;
	}

	public String getType() {
		return type;
	}
}
