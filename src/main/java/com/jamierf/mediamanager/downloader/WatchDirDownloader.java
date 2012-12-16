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
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatchDirDownloader implements Downloader {

    private final HttpClientFactory clientFactory;
	private final File watchDir;
    private final ExecutorService workerPool;

	public WatchDirDownloader(HttpClientFactory clientFactory, FileConfiguration config) {
        this.clientFactory = clientFactory;
        this.watchDir = config.getWatchDir();

        workerPool = Executors.newFixedThreadPool(config.getConcurrentDownloads());

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
	public void download(final URI link) throws IOException {
        final String filename = String.format("%s.torrent", Hashing.sha1().hashString(link.toString()).toString());

        workerPool.submit(new Callable<Object>() {
            @Override
            public Object call() throws IOException {
                final HttpClient client = clientFactory.build();

                final HttpResponse response = client.execute(new HttpGet(link));

                final InputStream in = response.getEntity().getContent();
                final FileOutputStream out = new FileOutputStream(new File(watchDir, filename));

                try {
                    IOUtils.copy(in, out);
                }
                finally {
                    in.close();
                    out.close();
                }

                return null;
            }
        });
	}
}
