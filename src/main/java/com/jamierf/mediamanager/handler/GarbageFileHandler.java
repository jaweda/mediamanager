package com.jamierf.mediamanager.handler;

import com.yammer.dropwizard.logging.Log;

import java.io.File;
import java.io.IOException;

public class GarbageFileHandler implements FileHandler {

    private static final Log LOG = Log.forClass(GarbageFileHandler.class);

    @Override
    public void handleFile(String relativePath, File file) throws IOException {
        if (LOG.isTraceEnabled())
            LOG.trace("Deleting garbage file");

        file.delete();
    }
}
