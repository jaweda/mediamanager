package com.jamierf.mediamanager.healthchecks;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.yammer.metrics.core.HealthCheck;

public class DatabaseHealthcheck extends HealthCheck {

    private final ShowDatabase shows;

    public DatabaseHealthcheck(ShowDatabase shows) {
        super("database");

        this.shows = shows;
    }

    @Override
    protected Result check() throws Exception {
        return shows.isConnected() ? Result.healthy() : Result.unhealthy("Database is not connected");
    }
}
