package com.jamierf.mediamanager.config;

import java.util.HashMap;

public class ParserConfiguration extends HashMap<String, Object> {

    public int getInt(String key) {
        return (Integer) super.get(key);
    }

    public String getString(String key) {
        return (String) super.get(key);
    }
}
