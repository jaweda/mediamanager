package com.jamierf.mediamanager.views;

import com.jamierf.mediamanager.models.Episode;
import io.dropwizard.views.View;

import java.util.Collection;

public class ShowsView extends View {

    private final Collection<Episode> episodes;

    public ShowsView(Collection<Episode> episodes) {
        super("shows.mustache");

        this.episodes = episodes;
    }

    public String getTitle() {
        return "Shows";
    }

    public Collection<Episode> getEpisodes() {
        return episodes;
    }
}
