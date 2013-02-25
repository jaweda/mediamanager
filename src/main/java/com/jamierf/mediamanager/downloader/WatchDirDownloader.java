package com.jamierf.mediamanager.downloader;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.yammer.dropwizard.client.JerseyClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.*;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchDirDownloader implements Downloader {

    private static String getFilename(URI link) {
        final String value = link.toString();
        final String hash = Hashing.sha1().hashString(value).toString();

        return String.format("%s.torrent", hash);
    }

    private final JerseyClient client;
    private final RetryManager retryManager;
    private final File watchDir;

	public WatchDirDownloader(JerseyClient client, RetryManager retryManager, File watchDir) {
        this.client = client;
        this.retryManager = retryManager;
        this.watchDir = watchDir;

        if (!watchDir.exists())
            watchDir.mkdirs();
	}

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    private InputStream openStream(final URI link) {
        return retryManager.apply(new Callable<InputStream>() {
            @Override
            public InputStream call() throws Exception {
                return client.resource(link).get(InputStream.class);
            }
        });
    }

	@Override
	public void download(final URI link) throws IOException {
        final String filename = WatchDirDownloader.getFilename(link);
        final File watchedFile = new File(watchDir, filename);

        try (
                final BufferedInputStream in = new BufferedInputStream(this.openStream(link));
                final FileOutputStream out = new FileOutputStream(watchedFile);
        ) {
            IOUtils.copy(in, out);
        }
	}
}
