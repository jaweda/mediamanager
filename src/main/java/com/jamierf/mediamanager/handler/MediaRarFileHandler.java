package com.jamierf.mediamanager.handler;

import com.jamierf.mediamanager.managers.DownloadDirManager;

import java.io.File;
import java.util.regex.Pattern;

public class MediaRarFileHandler extends RarFileHandler {

    private static final Pattern FILENAME_BLACKLIST_REGEX = Pattern.compile("(sample)", Pattern.CASE_INSENSITIVE);

    public MediaRarFileHandler(File destDir, boolean overwrite, boolean delete) {
        super(destDir, overwrite, delete);
    }

    @Override
    protected boolean acceptFile(String path) {
        // Check it's a media file type
        final String extension = DownloadDirManager.getFileExtension(path);
        if (!MediaFileHandler.EXTENSIONS.contains(extension))
            return false;

        // Check its name doesn't match the blacklist
        if (FILENAME_BLACKLIST_REGEX.matcher(path).matches())
            return false;

        return super.acceptFile(path);
    }

    @Override
    protected File getDestinationFile(String path) {
        // replace the path with the name only, i.e. extract all contents to the root
        if (path.contains(File.separator))
            path = path.substring(path.lastIndexOf(File.separator) + 1);

        return super.getDestinationFile(path);
    }
}
