package com.jamierf.mediamanager.config;

import java.io.File;
import java.util.HashMap;

public class DatabaseConfiguration extends HashMap<String, Object> {

    public long getLong(String key) {
        return (Long) super.get(key);
    }

    public String getString(String key) {
        return (String) super.get(key);
    }

    public File getFile(String key) {
        final String file = this.getString(key);
        return file == null ? null : new File(file);
    }
}
