package com.jamierf.mediamanager.db;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.config.DatabaseConfiguration;
import com.jamierf.mediamanager.models.Episode;
import com.sleepycat.je.*;
import com.yammer.dropwizard.json.Json;
import com.yammer.dropwizard.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class BDBShowDatabase implements ShowDatabase {

    private static final Log LOG = Log.forClass(BDBShowDatabase.class);

    private static final String DB_NAME = "shows.db";

    private static final Json JSON = new Json();

    private static DatabaseEntry createKey(Episode.Name name) {
        final String key = String.format("%s.%d.%d", name.getCleanedTitle(), name.getSeason(), name.getEpisode());
        return new DatabaseEntry(key.getBytes());
    }

    private final File file;

    private Environment env;
    private Database db;

    public BDBShowDatabase(DatabaseConfiguration config) {
        file = config.getFile("file");
        if (file == null)
            throw new IllegalArgumentException("No database file specified for BDB database");

        if (!file.exists()) {
            if (LOG.isDebugEnabled())
                LOG.debug("Creating BDB file at: {}", file);

            file.mkdirs();
        }
    }

    @Override
    public boolean addOrUpdate(Episode episode) {
        final DatabaseEntry key = BDBShowDatabase.createKey(episode.getName());
        final DatabaseEntry value = new DatabaseEntry(JSON.writeValueAsBytes(episode));

        final OperationStatus status = db.put(null, key, value);
        return status == OperationStatus.SUCCESS;
    }

    @Override
    public boolean addIfNotExists(Episode episode) {
        final DatabaseEntry key = BDBShowDatabase.createKey(episode.getName());
        final DatabaseEntry value = new DatabaseEntry(JSON.writeValueAsBytes(episode));

        final OperationStatus status = db.putNoOverwrite(null, key, value);
        return status == OperationStatus.SUCCESS;
    }

    @Override
    public Episode get(Episode.Name name) throws Exception {
        final DatabaseEntry key = BDBShowDatabase.createKey(name);
        final DatabaseEntry value = new DatabaseEntry();

        final OperationStatus status = db.get(null, key, value, LockMode.DEFAULT);
        if (status != OperationStatus.SUCCESS)
            return null;

        return JSON.readValue(value.getData(), Episode.class);
    }

    @Override
    public Collection<Episode> getAllEpisodes() {
        final ImmutableSet.Builder<Episode> episodes = ImmutableSet.builder();

        final Cursor cursor = db.openCursor(null, null);

        try {
            final DatabaseEntry key = new DatabaseEntry();
            final DatabaseEntry value = new DatabaseEntry();

            while (cursor.getNext(key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                try {
                    final Episode episode = JSON.readValue(value.getData(), Episode.class);
                    episodes.add(episode);
                }
                catch (IOException e) {
                    LOG.warn(e, "Failed to read row from database");
                }
            }
        }
        finally {
            cursor.close();
        }

        return episodes.build();
    }

    @Override
    public Collection<Episode> getDesiredEpisodes() {
        return Collections2.filter(this.getAllEpisodes(), new Predicate<Episode>() {
            @Override
            public boolean apply(Episode episode) {
                return episode.isDesired();
            }
        });
    }

    @Override
    public void start() throws Exception {
        env = new Environment(file, new EnvironmentConfig().setAllowCreate(true));
        db = env.openDatabase(null, DB_NAME, new DatabaseConfig().setAllowCreate(true));
    }

    @Override
    public boolean isConnected() {
        try {
            if (!env.isValid())
                return false;

            db.count();
            return true;
        }
        catch (DatabaseException e) {
            return false;
        }
    }

    @Override
    public void stop() throws Exception {
        db.close();
        env.close();
    }
}
