package com.jamierf.mediamanager.io.retry;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class InstrumentedRetryManager extends RetryManager {
    protected final Timer callTimer;
    protected final Meter retryMeter;

    public InstrumentedRetryManager(Class<?> klass) {
        callTimer = Metrics.newTimer(klass, "-calls");
        retryMeter = Metrics.newMeter(klass, "-retries", "attempts", TimeUnit.SECONDS);
    }

    @Override
    public <T> T apply(final Callable<T> callable) {
        final AtomicBoolean isRetry = new AtomicBoolean(false);
        TimerContext context = callTimer.time();
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
