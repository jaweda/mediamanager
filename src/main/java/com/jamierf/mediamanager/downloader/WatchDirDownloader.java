package com.jamierf.mediamanager.downloader;

import com.google.common.hash.Hashing;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.sun.jersey.api.client.Client;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class WatchDirDownloader implements Downloader {

    private static final Logger LOG = LoggerFactory.getLogger(WatchDirDownloader.class);

    private static String getFilename(URI link) {
        final String value = link.toString();
        final String hash = Hashing.sha1().hashString(value, StandardCharsets.UTF_8).toString();

        return String.format("%s.torrent", hash);
    }

    private final Client client;
    private final RetryManager retryManager;
    private final File watchDir;

	public WatchDirDownloader(Client client, RetryManager retryManager, File watchDir) {
        this.client = client;
        this.retryManager = retryManager;
        this.watchDir = watchDir;

        LOG.info("Using watch dir: {}", watchDir.getAbsolutePath());
        if (!watchDir.exists()) {
            if (LOG.isDebugEnabled())
                LOG.debug("Torrent watch directory '{}' doesn't exist, creating", watchDir);

            watchDir.mkdirs();
        }
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
