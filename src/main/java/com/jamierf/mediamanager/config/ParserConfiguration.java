package com.jamierf.mediamanager.config;

import java.util.HashMap;

public class ParserConfiguration extends HashMap<String, Object> {

    public long getLong(String key) {
        return (Long) super.get(key);
    }

    public String getString(String key) {
        return (String) super.get(key);
    }
}
