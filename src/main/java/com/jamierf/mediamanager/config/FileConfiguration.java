package com.jamierf.mediamanager.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Files;

import javax.validation.constraints.NotNull;
import java.io.File;

public class FileConfiguration {

    @JsonProperty
    @NotNull
    private File watchDir;

    @JsonProperty
    @NotNull
    private File destinationDir;

    @JsonProperty
    private File tempDir;

    @JsonProperty
    private boolean deleteOriginals = true;

    public File getWatchDir() {
        return watchDir;
    }

    public File getDestinationDir() {
        return destinationDir;
    }

    public File getTempDir() {
        if (tempDir == null)
            tempDir = Files.createTempDir();

        return tempDir;
    }

    public boolean isDeleteOriginals() {
        return deleteOriginals;
    }
}
