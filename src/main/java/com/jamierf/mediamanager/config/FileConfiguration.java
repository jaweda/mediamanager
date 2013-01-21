package com.jamierf.mediamanager.config;

import org.codehaus.jackson.annotate.JsonProperty;

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
    private boolean deleteOriginals = true;

    public File getWatchDir() {
        return watchDir;
    }

    public File getDestinationDir() {
        return destinationDir;
    }

    public boolean isDeleteOriginals() {
        return deleteOriginals;
    }
}
