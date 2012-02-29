package com.jamierf.mediamanager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamierf.epdirscanner.DirMonitor;
import com.jamierf.epdirscanner.FileListener;

public class DLManager {

	private static final Logger logger = LoggerFactory.getLogger(DLManager.class);

	private final DirMonitor monitor;
	private final Map<String, FileTypeHandler> fileHandlers;
	private final Collection<String> extras;
	private final int pathTrimLength;

	public DLManager(File downloadDir) throws IOException {
		monitor = new DirMonitor(downloadDir);

		monitor.addListener(new FileListener() {
			@Override
			public void onNewFile(File file) {
				processFile(file);
			}
		});

		fileHandlers = new HashMap<String, FileTypeHandler>();
		extras = new LinkedList<String>();
		pathTrimLength = downloadDir.getAbsolutePath().length();
	}

	public void addFileTypeHandler(FileTypeHandler handler) {
		final String[] exts = handler.getHandledExtensions();
		for (String ext : exts)
			fileHandlers.put(ext.toLowerCase(), handler);
	}

	public Collection<String> getExtras() {
		return extras;
	}

	public void start() {
		monitor.start();
	}

	public void stop() {
		monitor.stop();
	}

	private void processFile(File file) {
		final String path = file.getAbsolutePath().substring(pathTrimLength).replaceAll("\\\\", "/"); // trim the start then fix windows style slashes

		final String extension = EpisodeNamer.getFileExtension(path);
		if (extension == null) {
			if (logger.isDebugEnabled())
				logger.debug("Skipping file with no extension");

			extras.add(path);
			return;
		}

		final FileTypeHandler handler = fileHandlers.get(extension);
		if (handler == null) {
			if (logger.isTraceEnabled())
				logger.trace("Unhandled file type: " + extension);

			extras.add(path);
			return;
		}

		try {
			handler.handleFile(path, file);
		}
		catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn("Error handling file", e);

			extras.add(path);
		}
	}
}
