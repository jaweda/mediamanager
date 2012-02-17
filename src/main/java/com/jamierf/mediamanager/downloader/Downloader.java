package com.jamierf.mediamanager.downloader;

import java.io.IOException;
import java.net.URL;

public interface Downloader {
	public void download(URL link) throws IOException;
}
