package com.jamierf.mediamanager.parsing;

import com.jamierf.mediamanager.io.ParsedItem;

import java.net.URI;

public interface DownloadableItem extends ParsedItem {

    public String getTitle();
    public URI getLink();
}
