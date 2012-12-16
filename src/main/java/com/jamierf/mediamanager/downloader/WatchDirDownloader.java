package com.jamierf.mediamanager.downloader;

import com.google.common.hash.Hashing;
import com.jamierf.mediamanager.config.FileConfiguration;
import com.yammer.dropwizard.client.HttpClientFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WatchDirDownloader implements Downloader {

    private final HttpClientFactory clientFactory;
	private final File watchDir;

	public WatchDirDownloader(HttpClientFactory clientFactory, FileConfiguration config) {
        this.clientFactory = clientFactory;
        this.watchDir = config.getWatchDir();

        if (!watchDir.exists())
            watchDir.mkdirs();
	}

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

	@Override
	public void download(URL link) throws IOException {
        final String filename = String.format("%s.torrent", Hashing.sha1().hashString(link.toString()).toString());
        final HttpClient client = clientFactory.build();

        final HttpResponse response = client.execute(new HttpGet(link.toString()));

        final InputStream in = response.getEntity().getContent();
		final FileOutputStream out = new FileOutputStream(new File(watchDir, filename));

        try {
            IOUtils.copy(in, out);
        }
        finally {
            in.close();
            out.close();
        }
	}
}
