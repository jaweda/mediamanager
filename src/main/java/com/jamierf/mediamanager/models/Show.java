package com.jamierf.mediamanager.models;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.concurrent.Callable;

public class Show {

    @JsonIgnore
    private final Table<Integer, Integer, Episode> episodes;

    public Show() {
        episodes = HashBasedTable.create();
    }

    public Episode getOrCreateEpisode(int season, int episode) {
        if (!episodes.contains(season, episode))
            episodes.put(season, episode, new Episode(season, episode));

        return episodes.get(season, episode);
    }

    public Episode getEpisode(int season, int episode) {
        return episodes.get(season, episode);
    }

    @JsonProperty
    protected Collection<Episode> getEpisodes() {
        return episodes.values();
    }
}
