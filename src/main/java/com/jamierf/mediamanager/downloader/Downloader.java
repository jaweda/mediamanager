package com.jamierf.mediamanager.downloader;

import io.dropwizard.lifecycle.Managed;

import java.io.IOException;
import java.net.URI;

public interface Downloader extends Managed {
	public void download(URI link) throws IOException;
}
