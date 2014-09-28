package com.jamierf.mediamanager.io.retry;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class InstrumentedRetryManager extends RetryManager {
    protected final Timer callTimer;
    protected final Meter retryMeter;

    public InstrumentedRetryManager(final MetricRegistry metrics, final Class<?> klass) {
        callTimer = metrics.timer(MetricRegistry.name(klass, "-calls"));
        retryMeter = metrics.meter(MetricRegistry.name(klass, "-retries", "attempts"));
    }

    @Override
    public <T> T apply(final Callable<T> callable) {
        final AtomicBoolean isRetry = new AtomicBoolean(false);
        final Timer.Context context = callTimer.time();
        try {
            return super.apply(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    if (isRetry.getAndSet(true)) {
                        retryMeter.mark();
                    }
                    return callable.call();
                }
            });
        } finally {
            context.stop();
        }
    }
}
