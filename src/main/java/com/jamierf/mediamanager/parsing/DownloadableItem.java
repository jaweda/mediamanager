package com.jamierf.mediamanager.parsing;

import com.jamierf.mediamanager.io.ParsedItem;

import java.net.URL;

public interface DownloadableItem extends ParsedItem {

    public String getTitle();
    public URL getLink();
}
