package com.jamierf.mediamanager.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import io.dropwizard.util.Duration;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class TorrentConfiguration {

    @JsonProperty
    @NotNull
    private File watchDir;

    @JsonProperty
    private Duration updateDelay = Duration.minutes(15);

    @JsonProperty
    private Duration backfillDelay = Duration.days(7);

    @JsonProperty
    private Set<String> primaryQualities = EpisodeNameParser.EPISODE_QUALITIES;

    @JsonProperty
    private Duration primaryQualityTimeout = Duration.days(1);

    @JsonProperty
    private Set<String> secondaryQualities = Collections.emptySet();

    @JsonProperty
    private Map<String, ParserConfiguration> feeders = Collections.emptyMap();

    @JsonProperty
    private Map<String, ParserConfiguration> searchers = Collections.emptyMap();

    public File getWatchDir() {
        return watchDir;
    }

    public Duration getUpdateDelay() {
        return updateDelay;
    }

    public Duration getBackfillDelay() {
        return backfillDelay;
    }

    public Set<String> getPrimaryQualities() {
        return primaryQualities;
    }

    public Duration getPrimaryQualityTimeout() {
        return primaryQualityTimeout;
    }

    public Set<String> getSecondaryQualities() {
        return secondaryQualities;
    }

    public Map<String, ParserConfiguration> getFeeders() {
        return feeders;
    }

    public Map<String, ParserConfiguration> getSearchers() {
        return searchers;
    }
}
