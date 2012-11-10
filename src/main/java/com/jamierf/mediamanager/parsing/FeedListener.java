package com.jamierf.mediamanager.parsing;

public interface FeedListener<T extends FeedItem> {
	public void onNewItem(T item);
	public void onException(Throwable cause);
}
