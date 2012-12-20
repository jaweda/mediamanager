package com.jamierf.mediamanager.handler;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.yammer.dropwizard.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class MediaFileHandler implements FileTypeHandler {

	public static final ImmutableSet<String> EXTENSIONS = ImmutableSet.of("avi", "mkv", "mp4", "divx");

	private static final Log LOG = Log.forClass(MediaFileHandler.class);

    private final File destDir;
    private final boolean move;
	private final boolean overwrite;

	public MediaFileHandler(File destDir, boolean move, boolean overwrite) {
        this.destDir = destDir;
        this.move = move;
		this.overwrite = overwrite;

        if (!destDir.exists())
            destDir.mkdirs();
	}

	@Override
	public Collection<String> getHandledExtensions() {
		return EXTENSIONS;
	}

	@Override
	public void handleFile(String relativePath, File file) throws IOException {
		final File destFile = new File(destDir, file.getName());
		if (!overwrite && destFile.exists())
			throw new IOException("Skipping already existing media file: " + file.getName());

		// Make the parent directory if required
		final File destDir = destFile.getParentFile();
		if (!destDir.exists())
			destDir.mkdirs();

		if (move) {
			if (LOG.isTraceEnabled())
				LOG.trace("Moving {} to {}", relativePath, destFile.getAbsoluteFile());

            Files.move(file, destFile);
		}
		else {
			if (LOG.isTraceEnabled())
				LOG.trace("Copying {} to {}", relativePath, destFile.getAbsoluteFile());

            Files.copy(file, destFile);
		}
	}
}
