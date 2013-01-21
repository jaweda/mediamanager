package com.jamierf.mediamanager.handler;

import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.managers.DownloadDirManager;
import com.yammer.dropwizard.logging.Log;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class RarFileHandler implements FileTypeHandler {

	private static final ImmutableSet<String> EXTENSIONS = ImmutableSet.of("rar");
    private static final Pattern PATH_NORMALISATION_REGEX = Pattern.compile("[\\\\/]");

	private static final Log LOG = Log.forClass(RarFileHandler.class);

    protected final File destDir;
    protected final boolean delete;

    public RarFileHandler(File destDir, boolean delete) {
        this.destDir = destDir;
        this.delete = delete;

        if (!destDir.exists())
            destDir.mkdirs();
	}

	@Override
	public Collection<String> getHandledExtensions() {
		return EXTENSIONS;
	}

    protected boolean acceptContainedFile(String path) {
        return true;
    }

    protected File getDestinationFile(String path) {
        return new File(destDir.getAbsolutePath() + File.separator + path);
    }

    protected boolean extractFile(Archive archive, FileHeader fileHeader, File destFile) throws IOException, RarException {
        final OutputStream out = new FileOutputStream(destFile);

        try {
            archive.extractFile(fileHeader, out);
            return true;
        }
        finally {
            out.close();
        }
    }

	@Override
	public void handleFile(String relativePath, File file) throws IOException {
		try {
			final Archive archive = new Archive(file);

			int handled = 0;

			final List<FileHeader> fileHeaders = archive.getFileHeaders();
			for (FileHeader fileHeader : fileHeaders) {
                if (!this.acceptContainedFile(fileHeader.getFileNameString()))
                    continue;

                final String path = PATH_NORMALISATION_REGEX.matcher(fileHeader.getFileNameString()).replaceAll(File.separator);
                final File destFile = this.getDestinationFile(path);
				if (destFile.exists()) // Skip any existing files
					continue;

				if (LOG.isTraceEnabled())
					LOG.trace("Extracting {} to {}", fileHeader.getFileNameString(), destFile.getAbsoluteFile());

				// Make the parent directory if required
				final File destDir = destFile.getParentFile();
				if (!destDir.exists())
					destDir.mkdirs();

                if (this.extractFile(archive, fileHeader, destFile))
                    handled++;
			}

			if (handled < 1)
				throw new IOException("Skipping rar file with no handleable contents: " + file.getName());

            if (delete) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Deleting archive {} after extracting {} files", file.getName(), handled);

                // Delete the .rar
                file.delete();

                // Delete any related .r00 parts
                final String filename = DownloadDirManager.getFileName(file.getName());
                final Pattern pattern = Pattern.compile(Pattern.quote(filename) + "\\.(r\\d+)", Pattern.CASE_INSENSITIVE);
                for (File part : file.getParentFile().listFiles()) {
                    // Only delete files that are related rars
                    if (!pattern.matcher(part.getName()).matches())
                        continue;

                    if (LOG.isTraceEnabled())
                        LOG.trace("Deleting archive part {} after extracting files", part.getName());

                    part.delete();
                }
            }
		}
		catch (RarException e) {
			throw new IOException(e);
		}
	}
}
