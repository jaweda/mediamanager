package com.jamierf.mediamanager.handler;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.jamierf.mediamanager.db.FileDatabase;
import com.jamierf.mediamanager.listeners.MediaFileListener;

import java.io.File;
import java.io.IOException;

public class MediaRarFileHandler extends RarFileHandler {

    private final MediaFileListener listener;

    public MediaRarFileHandler(File destDir, boolean delete, MediaFileListener listener, FileDatabase files) {
        super(destDir, delete, files);

        this.listener = listener;
    }

    @Override
    protected boolean acceptContainedFile(String path) throws IOException {
        // Check our parent class will accept the file
        if (!super.acceptContainedFile(path))
            return false;

        // Check the Media file handler will accept the file
        if (!MediaFileHandler.acceptFileExtension(path))
            return false;

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
