package com.jamierf.mediamanager.listeners;

import com.jamierf.mediamanager.parsing.DownloadableItem;
import com.jamierf.mediamanager.parsing.ItemListener;

public class DownloadableItemListenerProxy<T extends DownloadableItem> implements ItemListener<T> {

    private final ItemListener<DownloadableItem> delegate;

    public DownloadableItemListenerProxy(ItemListener<DownloadableItem> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onNewItem(T item) {
        delegate.onNewItem(item);
    }

    @Override
    public void onException(Throwable cause) {
        delegate.onException(cause);
    }
}
