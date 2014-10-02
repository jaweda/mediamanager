package com.jamierf.mediamanager.handler;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.jamierf.mediamanager.db.FileDatabase;
import com.jamierf.mediamanager.listeners.MediaFileListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MediaRarFileHandler extends RarFileHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MediaRarFileHandler.class);

    private final MediaFileListener listener;

    public MediaRarFileHandler(File destDir, boolean delete, MediaFileListener listener, FileDatabase files) {
        super(destDir, delete, files);

        this.listener = listener;
    }

    @Override
    protected boolean acceptContainedFile(FileHeader file) throws IOException {
        // Check our parent class will accept the file
        if (!super.acceptContainedFile(file)) {
            return false;
        }

        final String path = file.getFileNameString();

        // Check the Media file handler will accept the file
        if (!MediaFileHandler.acceptFileExtension(path)) {
            LOG.trace("Rejecting unacceptable file extension: {}", file);
            return false;
        }

        final long size = file.getFullUnpackSize();
        if (!MediaFileHandler.acceptFileSize(size)) {
            LOG.trace("Rejecting too small file: {} ({} bytes)", file, size);
            return false;
        }

        return true;
    }

    @Override
    protected File getDestinationFile(String path) {
        // replace the path with the name only, i.e. extract all contents to the root
        if (path.contains(File.separator))
            path = path.substring(path.lastIndexOf(File.separator) + 1);

        return super.getDestinationFile(path);
    }

    @Override
    protected boolean extractFile(Archive archive, FileHeader fileHeader, File destFile) throws IOException {
        final boolean success = super.extractFile(archive, fileHeader, destFile);
        if (success)
            listener.onNewItem(destFile);

        return success;
    }
}
