package com.jamierf.mediamanager.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.jamierf.mediamanager.db.ShowDatabase;

public class DatabaseHealthcheck extends HealthCheck {

    private final ShowDatabase shows;

    public DatabaseHealthcheck(ShowDatabase shows) {
        this.shows = shows;
    }

    @Override
    protected Result check() throws Exception {
        return shows.isConnected() ? Result.healthy() : Result.unhealthy("Database is not connected");
    }
}
