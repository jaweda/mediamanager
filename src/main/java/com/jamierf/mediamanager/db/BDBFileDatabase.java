package com.jamierf.mediamanager.db;

import java.io.File;
import java.io.IOException;

public class BDBFileDatabase extends BDBDatabase<String, Boolean> implements FileDatabase {

    public BDBFileDatabase(File file) {
        super (file, "file.db", Boolean.class);
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
