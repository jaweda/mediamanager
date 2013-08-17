package com.jamierf.mediamanager.db;

import com.jamierf.mediamanager.config.DatabaseConfiguration;

import java.io.IOException;

public class BDBFileDatabase extends BDBDatabase<String, Boolean> implements FileDatabase {

    public BDBFileDatabase(DatabaseConfiguration config) {
        super (config.getFile("file"), "file.db", Boolean.class);
    }

    @Override
    public boolean addHandled(String name) throws IOException {
        return this.addOrUpdate(name, Boolean.TRUE);
    }

    @Override
    public boolean isHandled(String name) throws IOException {
        return this.get(name).or(Boolean.FALSE).booleanValue();
    }
}
