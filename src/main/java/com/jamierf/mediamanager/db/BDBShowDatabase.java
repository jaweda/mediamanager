package com.jamierf.mediamanager.db;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.jamierf.mediamanager.config.DatabaseConfiguration;
import com.jamierf.mediamanager.models.Episode;

import java.io.IOException;
import java.util.Collection;

public class BDBShowDatabase extends BDBDatabase<String, Episode> implements ShowDatabase {

    private static String createKey(Episode.Name name) {
        return name.toString();
    }

    public BDBShowDatabase(DatabaseConfiguration config) {
        super (config.getFile("file"), "shows.db", Episode.class);
    }

    @Override
    public boolean addOrUpdate(Episode episode) throws IOException {
        final String key = BDBShowDatabase.createKey(episode.getName());
        return this.addOrUpdate(key, episode);
    }

    @Override
    public boolean addIfNotExists(Episode episode) throws IOException {
        final String key = BDBShowDatabase.createKey(episode.getName());
        return this.addIfNotExists(key, episode);
    }

    @Override
    public Optional<Episode> get(Episode.Name name) throws IOException {
        final String key = BDBShowDatabase.createKey(name);
        return super.get(key);
    }

    @Override
    public Collection<Episode> getAllEpisodes() throws IOException {
        return this.getAll();
    }

    @Override
    public Collection<Episode> getDesiredEpisodes() throws IOException {
        return Collections2.filter(this.getAll(), new Predicate<Episode>() {
            @Override
            public boolean apply(Episode episode) {
                return episode.isDesired();
            }
        });
    }
}
