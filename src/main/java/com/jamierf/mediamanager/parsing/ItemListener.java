package com.jamierf.mediamanager.parsing;

import com.jamierf.mediamanager.io.ParsedItem;

public interface ItemListener<T extends ParsedItem> {
	public void onNewItem(T item);
	public void onException(Throwable cause);
}
