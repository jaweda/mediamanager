package com.jamierf.mediamanager.db;

import com.google.common.base.Optional;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.Name;
import com.yammer.dropwizard.lifecycle.Managed;

import java.io.IOException;
import java.util.Collection;

public interface ShowDatabase extends Managed {
    public boolean addOrUpdate(Episode episode) throws IOException;
    public boolean addIfNotExists(Episode episode) throws IOException;
    public Optional<Episode> get(Name name) throws IOException;
    public Collection<Episode> getAllEpisodes() throws IOException;
    public Collection<Episode> getDesiredEpisodes() throws IOException;
    public boolean isConnected();
}
