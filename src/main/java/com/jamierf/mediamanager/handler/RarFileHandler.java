package com.jamierf.mediamanager.handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamierf.epdirscanner.FilenameParser;
import com.jamierf.mediamanager.EpisodeNamer;
import com.jamierf.mediamanager.FileTypeHandler;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class RarFileHandler implements FileTypeHandler {

	private static final String[] EXTENSIONS = { "rar" };

	private static final Logger logger = LoggerFactory.getLogger(RarFileHandler.class);

	private final EpisodeNamer namer;

	public RarFileHandler(EpisodeNamer namer) {
		this.namer = namer;
	}

	@Override
	public String[] getHandledExtensions() {
		return EXTENSIONS;
	}

	@Override
	public void handleFile(File file) throws IOException {
		try {
			final Archive archive = new Archive(file);

			final List<FileHeader> fileHeaders = archive.getFileHeaders();
			for (FileHeader fileHeader : fileHeaders) {
				final FilenameParser.Parts parts = FilenameParser.parse(fileHeader.getFileNameString());
				if (parts == null) {
					if (logger.isDebugEnabled())
						logger.debug("Skipping unparsable rar file contents: " + fileHeader.getFileNameString());

					continue;
				}

				final File destFile = namer.getEpisodeFile(fileHeader.getFileNameString(), parts.getTitle(), parts.getSeason(), parts.getEpisode());
				if (destFile.exists())
					return;

				if (logger.isTraceEnabled())
					logger.trace("Extracting {} to {}", parts, destFile.getAbsoluteFile());

				// Make the parent directory if required
				final File destDir = destFile.getParentFile();
				if (!destDir.exists())
					destDir.mkdirs();

				final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));

				try {
					archive.extractFile(fileHeader, out);
				}
				finally {
					out.close();
				}
			}
		}
		catch (RarException e) {
			throw new IOException(e);
		}
	}
}
