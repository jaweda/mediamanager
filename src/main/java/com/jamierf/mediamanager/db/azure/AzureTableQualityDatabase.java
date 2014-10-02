package com.jamierf.mediamanager.db.azure;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.jamierf.mediamanager.db.QualityDatabase;
import com.jamierf.mediamanager.models.Name;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import org.joda.time.DateTime;

import java.util.Map;

public class AzureTableQualityDatabase implements QualityDatabase {

    private static final String TABLE_NAME = "quality";

    private final Map<Name, DateTime> shows;

    public AzureTableQualityDatabase(final String accountName, final String accountKey, final MetricRegistry metrics) throws StorageException {
        shows = new AzureMap<>(accountName, accountKey, TABLE_NAME, Name.class, DateTime.class, metrics);
    }

    @Override
    public void update(final Name name, final DateTime time) {
        shows.putIfAbsent(name, time);
    }

    @Override
    public Optional<DateTime> get(final Name name) {
        return Optional.fromNullable(shows.get(name));
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
