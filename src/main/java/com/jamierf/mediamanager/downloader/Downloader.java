package com.jamierf.mediamanager.downloader;

import com.yammer.dropwizard.lifecycle.Managed;

import java.io.IOException;
import java.net.URL;

public interface Downloader extends Managed {
	public void download(URL link) throws IOException;
}
