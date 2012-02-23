package com.jamierf.mediamanager.handler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamierf.epdirscanner.FilenameParser;
import com.jamierf.mediamanager.EpisodeNamer;
import com.jamierf.mediamanager.FileTypeHandler;

public class MediaFileHandler implements FileTypeHandler {

	private static final String[] EXTENSIONS = { "avi", "mkv" };

	private static final Logger logger = LoggerFactory.getLogger(MediaFileHandler.class);

	private final EpisodeNamer namer;
	private final boolean move;
	private final boolean overwrite;

	public MediaFileHandler(EpisodeNamer namer, boolean move, boolean overwrite) {
		this.namer = namer;
		this.move = move;
		this.overwrite = overwrite;
	}

	@Override
	public String[] getHandledExtensions() {
		return EXTENSIONS;
	}

	@Override
	public void handleFile(File file) throws IOException {
		final FilenameParser.Parts parts = FilenameParser.parse(file.getName());
		if (parts == null) {
			if (logger.isDebugEnabled())
				logger.debug("Skipping unparsable media file: " + file.getName());

			return;
		}

		final File destFile = namer.getEpisodeFile(file.getName(), parts.getTitle(), parts.getSeason(), parts.getEpisode());
		if (!overwrite && destFile.exists())
			return;

		// Make the parent directory if required
		final File destDir = destFile.getParentFile();
		if (!destDir.exists())
			destDir.mkdirs();

		if (move) {
			if (logger.isTraceEnabled())
				logger.trace("Moving {} to {}", parts, destFile.getAbsoluteFile());

			FileUtils.moveFile(file, destFile);
		}
		else {
			if (logger.isTraceEnabled())
				logger.trace("Copying {} to {}", parts, destFile.getAbsoluteFile());

			FileUtils.copyFile(file, destFile);
		}
	}
}
