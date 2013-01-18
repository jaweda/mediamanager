package com.jamierf.mediamanager.config;

import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import org.codehaus.jackson.annotate.JsonProperty;

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
}
