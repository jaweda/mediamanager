package com.jamierf.mediamanager.io.retry;

import com.google.common.base.Throwables;

import java.util.concurrent.Callable;

abstract public class RetryManager {
    abstract protected boolean retryFailure(Exception e, int attempts, long startTimeMS);

    public <T> T apply(Callable<T> callable) {
        long startTimeMS = System.currentTimeMillis();
        int attempts = 0;
        while (true) {
            try {
                ++attempts;
                return callable.call();
            } catch (Exception e) {
                if (!retryFailure(e, attempts, startTimeMS)) {
                    Throwables.propagateIfPossible(e);
                    throw Throwables.propagate(e);
                }
            }
        }
    }
}
