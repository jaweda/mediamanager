package com.jamierf.mediamanager.db;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.jamierf.mediamanager.config.DatabaseConfiguration;
import com.jamierf.mediamanager.models.Episode;

import java.io.IOException;
import java.util.Collection;

public class BDBShowDatabase extends BDBDatabase<Episode.Name, Episode> implements ShowDatabase {

    public BDBShowDatabase(DatabaseConfiguration config) {
        super (config.getFile("file"), "shows.db", Episode.class);
    }

    @Override
    public boolean addOrUpdate(Episode episode) throws IOException {
        return this.addOrUpdate(episode.getName(), episode);
    }

    @Override
    public boolean addIfNotExists(Episode episode) throws IOException {
        return this.addIfNotExists(episode.getName(), episode);
    }

    @Override
    public Optional<Episode> get(Episode.Name key) throws IOException {
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
