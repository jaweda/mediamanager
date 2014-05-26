package com.jamierf.mediamanager.db.azure;

import com.jamierf.mediamanager.db.FileDatabase;
import com.microsoft.windowsazure.services.core.storage.StorageException;

import java.io.IOException;
import java.util.Set;

public class AzureTableFileDatabase implements FileDatabase {

    private static final String TABLE_NAME = "file";

    private final Set<String> files;

    public AzureTableFileDatabase(String accountName, String accountKey) throws StorageException {
        files = new AzureSet<>(accountName, accountKey, TABLE_NAME, String.class);
    }

    @Override
    public boolean addHandled(String name) throws IOException {
        files.add(name);
        return true;
    }

    @Override
    public boolean isHandled(String name) throws IOException {
        return files.contains(name);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
