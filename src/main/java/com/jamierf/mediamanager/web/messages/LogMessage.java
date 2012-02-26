package com.jamierf.mediamanager.web.messages;

public class LogMessage {

	public enum Type { DEBUG, DOWNLOAD };

	private final String message;
	private final Type type;

	public LogMessage(String message, Type type) {
		this.message = message;
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public Type getType() {
		return type;
	}
}
