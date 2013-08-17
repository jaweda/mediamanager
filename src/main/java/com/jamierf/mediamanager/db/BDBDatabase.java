package com.jamierf.mediamanager.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sleepycat.je.*;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.lifecycle.Managed;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class BDBDatabase<K, V> implements Managed {

    private static final ObjectMapper JSON = new ObjectMapperFactory().build();

    private final File file;
    private final String name;
    private final Class<V> valueClass;

    private Environment env;
    private Database db;

    protected BDBDatabase(File file, String name, Class<V> valueClass) {
        this.file = file;
        this.name = name;
        this.valueClass = valueClass;
    }

    protected DatabaseEntry writeKey(K key) throws IOException {
        return new DatabaseEntry(JSON.writeValueAsBytes(key));
    }

    protected DatabaseEntry writeValue(V value) throws IOException {
        return new DatabaseEntry(JSON.writeValueAsBytes(value));
    }

    protected V readValue(DatabaseEntry entry) throws IOException {
        return JSON.readValue(entry.getData(), valueClass);
    }

    @Override
    public void start() throws Exception {
        env = new Environment(file, new EnvironmentConfig().setAllowCreate(true));
        db = env.openDatabase(null, name, new DatabaseConfig().setAllowCreate(true));
    }

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

    protected boolean addOrUpdate(K key, V value) throws IOException {
        final DatabaseEntry bdbKey = this.writeKey(key);
        final DatabaseEntry bdbValue = this.writeValue(value);

        final OperationStatus status = db.put(null, bdbKey, bdbValue);
        return status == OperationStatus.SUCCESS;
    }

    protected boolean addIfNotExists(K key, V value) throws IOException {
        final DatabaseEntry bdbKey = this.writeKey(key);
        final DatabaseEntry bdbValue = this.writeValue(value);

        final OperationStatus status = db.putNoOverwrite(null, bdbKey, bdbValue);
        return status == OperationStatus.SUCCESS;
    }

    protected Optional<V> get(K key) throws IOException {
        final DatabaseEntry bdbKey = this.writeKey(key);
        final DatabaseEntry bdbValue = new DatabaseEntry();

        final OperationStatus status = db.get(null, bdbKey, bdbValue, LockMode.DEFAULT);
        if (status != OperationStatus.SUCCESS)
            return Optional.absent();

        return Optional.fromNullable(this.readValue(bdbValue));
    }

    protected List<V> getAll() throws IOException {
        final ImmutableList.Builder<V> results = ImmutableList.builder();

        final Cursor cursor = db.openCursor(null, null);

        try {
            final DatabaseEntry bdbKey = new DatabaseEntry();
            final DatabaseEntry bdbValue = new DatabaseEntry();

            while (cursor.getNext(bdbKey, bdbValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                final V value = this.readValue(bdbValue);
                results.add(value);
            }
        }
        finally {
            cursor.close();
        }

        return results.build();
    }

    @Override
    public void stop() throws Exception {
        db.close();
        env.close();
    }
}
