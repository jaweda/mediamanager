package com.jamierf.mediamanager.db.azure;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.Name;
import com.microsoft.windowsazure.services.core.storage.StorageException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class AzureTableShowDatabase implements ShowDatabase {

    private static final String TABLE_NAME = "show";

    private final Map<Name, Episode> episodes;

    public AzureTableShowDatabase(final String accountName, final String accountKey, final MetricRegistry metrics) throws StorageException {
        episodes = new AzureMap<>(accountName, accountKey, TABLE_NAME, Name.class, Episode.class, metrics);
    }

    @Override
    public boolean addOrUpdate(Episode episode) throws IOException {
        final Name key = episode.getName();

        episodes.put(key, episode);
        return true;
    }

    @Override
    public boolean addIfNotExists(Episode episode) throws IOException {
        final Name key = episode.getName();

        if (!episodes.containsKey(key)) {
            episodes.put(key, episode);
            return true;
        }

        return false;
    }

    @Override
    public Optional<Episode> get(Name name) throws IOException {
        return Optional.fromNullable(episodes.get(name));
    }

    @Override
    public Collection<Episode> getAllEpisodes() throws IOException {
        return episodes.values();
    }

    @Override
    public Collection<Episode> getDesiredEpisodes() throws IOException {
        return Collections2.filter(this.getAllEpisodes(), new Predicate<Episode>() {
            @Override
            public boolean apply(Episode episode) {
                return episode.isDesired();
            }
        });
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
