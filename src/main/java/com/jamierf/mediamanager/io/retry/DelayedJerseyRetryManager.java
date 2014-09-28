package com.jamierf.mediamanager.io.retry;

import com.codahale.metrics.MetricRegistry;
import com.jamierf.mediamanager.config.RetryConfiguration;
import io.dropwizard.util.Duration;

import java.security.SecureRandom;
import java.util.Set;

public class DelayedJerseyRetryManager extends JerseyRetryManager {
    protected final Duration wait;
    protected final boolean exponential;
    protected final SecureRandom randomize;

    public DelayedJerseyRetryManager(final MetricRegistry metrics, final Class<?> klass, final RetryConfiguration config) {
        this (metrics, klass, config.getMaxRetries(),
                config.getMaxDuration(), config.getRetriableStatusCodes(),
                config.getWait(), config.isExponentialBackoff(), config.isRandomiseWait());
    }

    public DelayedJerseyRetryManager(final MetricRegistry metrics, final Class<?> klass, final int maxRetries,
                                     final Duration maxDuration, final Set<Integer> retriableStatusCodes,
                                     final Duration wait, final boolean exponential, final boolean randomize) {
        super (metrics, klass, maxRetries, maxDuration, retriableStatusCodes);

        this.wait = wait;
        this.exponential = exponential;
        this.randomize = randomize ? new SecureRandom() : null;
    }

    @Override
    protected boolean retryFailure(final Exception exception, final int attempts, final long startTimeMS) {
        if (super.retryFailure(exception, attempts, startTimeMS)) {
            long naptime = wait.toMilliseconds();

            if (exponential)
                naptime *= (1 << attempts);

            if (randomize != null)
                naptime = (naptime * (1 + randomize.nextInt(1000)))/1000;

            try {
                Thread.sleep(naptime);
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
