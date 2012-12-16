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
    private int concurrentDownloads = 10;

    @JsonProperty
    private boolean overwriteFiles = false;

    @JsonProperty
    private boolean moveFiles = true;

    @JsonProperty
    private boolean deleteArchives = true;

    public File getWatchDir() {
        return watchDir;
    }

    public File getDestinationDir() {
        return destinationDir;
    }

    public int getConcurrentDownloads() {
        return concurrentDownloads;
    }

    public boolean isOverwriteFiles() {
        return overwriteFiles;
    }

    public boolean isMoveFiles() {
        return moveFiles;
    }

    public boolean isDeleteArchives() {
        return deleteArchives;
    }
}
