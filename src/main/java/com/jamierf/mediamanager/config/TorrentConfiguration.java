package com.jamierf.mediamanager.config;

import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.yammer.dropwizard.util.Duration;
import org.codehaus.jackson.annotate.JsonProperty;

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
    private Set<String> qualities = EpisodeNameParser.EPISODE_QUALITIES;

    @JsonProperty
    private Map<String, ParserConfiguration> parsers = Collections.emptyMap();

    public File getWatchDir() {
        return watchDir;
    }

    public Duration getUpdateDelay() {
        return updateDelay;
    }

    public Set<String> getQualities() {
        return qualities;
    }

    public Map<String, ParserConfiguration> getParsers() {
        return parsers;
    }
}
