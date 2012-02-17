package com.jamierf.mediamanager.downloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;


public class WatchDirDownloader implements Downloader {

	private static final int CONNECTION_TIMEOUT = 10; // seconds

	private final File watchDir;

	public WatchDirDownloader(File watchDir) {
		this.watchDir = watchDir;
	}

	@Override
	public void download(URL link) throws IOException {
		final String filename = new File(link.getPath()).getName();

		final FileOutputStream out = new FileOutputStream(new File(watchDir, filename));
		final BufferedInputStream in = new BufferedInputStream(this.openConnection(link).getInputStream());

		try {
			byte[] buffer = new byte[1024];
			int size = 0;
			while ((size = in.read(buffer)) > -1)
				out.write(buffer, 0, size);

		}
		finally {
			in.close();
			out.close();
		}
	}

	protected URLConnection openConnection(URL url) throws IOException {
		final URLConnection conn = url.openConnection();

		conn.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT));

		return conn;
	}
}
