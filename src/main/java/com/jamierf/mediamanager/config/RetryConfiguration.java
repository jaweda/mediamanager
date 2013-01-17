package com.jamierf.mediamanager.config;

import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.io.retry.JerseyRetryManager;
import com.yammer.dropwizard.util.Duration;

public class RetryConfiguration {

    private int maxRetries = 10;

    private Duration maxDuration = JerseyRetryManager.DEFAULT_MAX_DURATION;

    private ImmutableSet<Integer> retriableStatusCodes = JerseyRetryManager.DEFAULT_RETRIABLE_STATUS_CODES;

    private Duration wait = Duration.seconds(2);

    private boolean exponentialBackoff = true;

    private boolean randomiseWait = true;

    public int getMaxRetries() {
        return maxRetries;
    }

    public Duration getMaxDuration() {
        return maxDuration;
    }

    public ImmutableSet<Integer> getRetriableStatusCodes() {
        return retriableStatusCodes;
    }

    public Duration getWait() {
        return wait;
    }

    public boolean isExponentialBackoff() {
        return exponentialBackoff;
    }

    public boolean isRandomiseWait() {
        return randomiseWait;
    }
}
