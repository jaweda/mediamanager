package com.jamierf.mediamanager.config;

import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.yammer.dropwizard.client.HttpClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.util.Duration;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Map;
import java.util.Set;

public class MediaConfiguration extends Configuration {

    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    @JsonProperty
    private boolean overwriteFiles = false;

    @JsonProperty
    private boolean moveFiles = true;

    @JsonProperty
    private boolean deleteArchives = true;

    @JsonProperty
    @NotNull
    private File mediaDir;

    @JsonProperty
    @NotNull
    private File torrentWatchDir;

    @JsonProperty
    @NotNull
    private File torrentDownloadDir;

    @JsonProperty
    private Duration calendarUpdateDelay = Duration.hours(12);

    @JsonProperty
    private Duration torrentUpdateDelay = Duration.minutes(15);

    @JsonProperty
    private Set<String> desiredQualities = EpisodeNameParser.EPISODE_QUALITIES;

    @JsonProperty
    private Duration beforeAirDuration = Duration.hours(2);

    @JsonProperty
    private Duration afterAirDuration = Duration.days(1);

    @JsonProperty
    @NotNull
    private Map<String, ParserConfiguration> rssParsers;

    @JsonProperty
    @NotNull
    private Map<String, ParserConfiguration> calendarParsers;

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
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

    public File getMediaDir() {
        return mediaDir;
    }

    public File getTorrentWatchDir() {
        return torrentWatchDir;
    }

    public File getTorrentDownloadDir() {
        return torrentDownloadDir;
    }

    public Duration getCalendarUpdateDelay() {
        return calendarUpdateDelay;
    }

    public Duration getTorrentUpdateDelay() {
        return torrentUpdateDelay;
    }

    public Set<String> getDesiredQualities() {
        return desiredQualities;
    }

    public Duration getBeforeAirDuration() {
        return beforeAirDuration;
    }

    public Duration getAfterAirDuration() {
        return afterAirDuration;
    }

    public Map<String, ParserConfiguration> getRssParsers() {
        return rssParsers;
    }

    public Map<String, ParserConfiguration> getCalendarParsers() {
        return calendarParsers;
    }
}
