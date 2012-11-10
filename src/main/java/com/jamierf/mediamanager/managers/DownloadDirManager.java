package com.jamierf.mediamanager.managers;

import com.google.common.collect.Maps;
import com.jamierf.mediamanager.handler.FileHandler;
import com.jamierf.mediamanager.handler.FileTypeHandler;
import com.jamierf.mediamanager.io.DirMonitor;
import com.jamierf.mediamanager.io.FileListener;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.logging.Log;
import de.innosystec.unrar.rarfile.FileHeader;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadDirManager implements FileListener, Managed {

	private static final Log LOG = Log.forClass(DownloadDirManager.class);

    public static String getFileExtension(String name) {
        if (!name.contains("."))
            return "";

        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }

	private final DirMonitor monitor;
	private final Map<String, FileTypeHandler> fileHandlers;
	private final int pathTrimLength;
    private FileHandler defaultHandler;

	public DownloadDirManager(File downloadDir) throws IOException {
        if (!downloadDir.exists())
            downloadDir.mkdirs();

		monitor = new DirMonitor(downloadDir);
		monitor.addListener(this);

		fileHandlers = Maps.newHashMap();
		pathTrimLength = downloadDir.getAbsolutePath().length();

        defaultHandler = null;
	}

	public void addFileTypeHandler(FileTypeHandler handler) {
		for (String ext : handler.getHandledExtensions())
			fileHandlers.put(ext.toLowerCase(), handler);
	}

    public void setDefaultFileHandler(FileHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    @Override
	public void start() {
		monitor.start();
	}

    @Override
	public void stop() {
		monitor.stop();
	}

    private FileHandler getFileHandler(String path) {
        final String extension = DownloadDirManager.getFileExtension(path);

        // If we have a handler for this extension, use that
        if (fileHandlers.containsKey(extension))
            return fileHandlers.get(extension);

        // Use the default handler
        return defaultHandler;
    }

    @Override
	public void onNewFile(File file) {
		final String path = file.getAbsolutePath().substring(pathTrimLength).replaceAll("\\\\", "/"); // trim the start then fix windows style slashes

		try {
            final FileHandler handler = this.getFileHandler(path);
            if (handler == null) {
                if (LOG.isTraceEnabled())
                    LOG.trace("Unhandled file: " + path);

                return;
            }

			handler.handleFile(path, file);
		}
		catch (Exception e) {
            LOG.warn(e, "Error handling file");
		}
	}
}
