package com.jamierf.mediamanager.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;

import java.util.Collections;
import java.util.Map;

public class MediaManagerConfiguration extends Configuration {

    @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    @JsonProperty
    private RetryConfiguration retryManager = new RetryConfiguration();

    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private TorrentConfiguration torrents = new TorrentConfiguration();

    @JsonProperty
    private CalendarConfiguration calendars = new CalendarConfiguration();

    @JsonProperty
    private FileConfiguration files = new FileConfiguration();

    @JsonProperty
    private Map<String, String> aliases = Collections.emptyMap();

    public JerseyClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    public RetryConfiguration getRetryConfiguration() {
        return retryManager;
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        return database;
    }

    public TorrentConfiguration getTorrentConfiguration() {
        return torrents;
    }

    public CalendarConfiguration getCalendarConfiguration() {
        return calendars;
    }

    public FileConfiguration getFileConfiguration() {
        return files;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }
}
