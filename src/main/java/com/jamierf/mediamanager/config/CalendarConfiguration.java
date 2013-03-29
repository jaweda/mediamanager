package com.jamierf.mediamanager.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.util.Duration;

import java.util.Collections;
import java.util.Map;

public class CalendarConfiguration {

    @JsonProperty
    private Duration updateDelay = Duration.hours(12);

    @JsonProperty
    private Duration beforeAirDuration = Duration.hours(2);

    @JsonProperty
    private Duration afterAirDuration = Duration.days(1);

    @JsonProperty
    private Map<String, ParserConfiguration> parsers = Collections.emptyMap();

    public Duration getUpdateDelay() {
        return updateDelay;
    }

    public Duration getBeforeAirDuration() {
        return beforeAirDuration;
    }

    public Duration getAfterAirDuration() {
        return afterAirDuration;
    }

    public Map<String, ParserConfiguration> getParsers() {
        return parsers;
    }
}
