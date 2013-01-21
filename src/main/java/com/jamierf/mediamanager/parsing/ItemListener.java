package com.jamierf.mediamanager.parsing;

public interface ItemListener<T> {
	public void onNewItem(T item);
	public void onException(Throwable cause);
}
