package com.jamierf.mediamanager.handler;

import java.io.File;

public class MediaRarFileHandler extends RarFileHandler {

    public MediaRarFileHandler(File destDir, boolean overwrite, boolean delete) {
        super(destDir, overwrite, delete);
    }

    @Override
    protected boolean acceptContainedFile(String path) {
        // Check our parent class will accept the file
        if (!super.acceptContainedFile(path))
            return false;

        // Check the Media file handler will accept the file
        if (!MediaFileHandler.acceptFile(path))
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
}
