package com.jamierf.mediamanager.db;

import com.jamierf.mediamanager.models.Episode;
import com.yammer.dropwizard.lifecycle.Managed;

import java.util.Collection;
import java.util.regex.Pattern;

public interface ShowDatabase extends Managed {
    public boolean addOrUpdate(Episode episode);
    public boolean addIfNotExists(Episode episode) throws Exception;
    public Episode get(Episode.Name name) throws Exception;
    public Collection<Episode> getAllEpisodes();
    public Collection<Episode> getDesiredEpisodes();
    public boolean isConnected();
}
