package com.jamierf.mediamanager.handler;

import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.logging.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class GarbageFileHandler implements FileTypeHandler {

    public static final ImmutableSet<String> EXTENSIONS = ImmutableSet.of("txt", "nfo", "sfv");

    private static final Log LOG = Log.forClass(GarbageFileHandler.class);

    @Override
    public Collection<String> getHandledExtensions() {
        return EXTENSIONS;
    }

    @Override
    public void handleFile(String relativePath, File file) throws IOException {
        // Only delete directories if they are empty
        if (file.isDirectory() && file.list().length > 0)
            return;

        if (LOG.isTraceEnabled())
            LOG.trace("Deleting garbage file: {}", file.getName());

        file.delete();
    }
}
