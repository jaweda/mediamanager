package com.jamierf.mediamanager.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.yammer.dropwizard.util.Duration;

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
    private Set<String> qualities = EpisodeNameParser.EPISODE_QUALITIES;

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

    public Set<String> getQualities() {
        return qualities;
    }

    public Map<String, ParserConfiguration> getFeeders() {
        return feeders;
    }

    public Map<String, ParserConfiguration> getSearchers() {
        return searchers;
    }
}
