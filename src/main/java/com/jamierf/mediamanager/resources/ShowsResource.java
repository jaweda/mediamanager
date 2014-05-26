package com.jamierf.mediamanager.resources;

import com.google.common.collect.Lists;
import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.views.ShowsView;
import io.dropwizard.views.View;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Path("/shows")
public class ShowsResource {

    private final ShowDatabase shows;

    public ShowsResource(ShowDatabase shows) {
        this.shows = shows;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public View getShowsDatabase() throws IOException {
        final List<Episode> episodes = Lists.newArrayList(shows.getAllEpisodes());
        Collections.sort(episodes);

        return new ShowsView(episodes);
    }
}
