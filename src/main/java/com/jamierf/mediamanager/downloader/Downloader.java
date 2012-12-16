package com.jamierf.mediamanager.downloader;

import com.yammer.dropwizard.lifecycle.Managed;

import java.io.IOException;
import java.net.URI;

public interface Downloader extends Managed {
	public void download(URI link) throws IOException;
}
