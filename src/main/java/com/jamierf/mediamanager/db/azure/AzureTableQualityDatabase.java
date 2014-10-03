package com.jamierf.mediamanager.db.azure;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.jamierf.mediamanager.db.QualityDatabase;
import com.jamierf.mediamanager.models.Name;
import com.jamierf.mediamanager.util.TimestampToDateTimeFunction;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import org.joda.time.DateTime;

import java.util.Map;

public class AzureTableQualityDatabase implements QualityDatabase {

    private static final String TABLE_NAME = "seen";

    private final Map<Name, Long> shows;

    public AzureTableQualityDatabase(final String accountName, final String accountKey, final MetricRegistry metrics) throws StorageException {
        shows = new AzureMap<>(accountName, accountKey, TABLE_NAME, Name.class, Long.class, metrics);
    }

    @Override
    public void update(final Name name, final DateTime time) {
        shows.putIfAbsent(name, time.getMillis());
    }

    @Override
    public Optional<DateTime> get(final Name name) {
        return Optional.fromNullable(shows.get(name)).transform(TimestampToDateTimeFunction.INSTANCE);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
