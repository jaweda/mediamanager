package com.jamierf.mediamanager.db;

import com.yammer.dropwizard.lifecycle.Managed;

import java.io.IOException;

public interface FileDatabase extends Managed {
    public boolean addHandled(String name) throws IOException;
    public boolean isHandled(String name) throws IOException;
}
