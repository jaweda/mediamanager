package com.jamierf.mediamanager.resources;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.views.ShowsView;
import com.yammer.dropwizard.views.View;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/shows")
public class ShowsResource {

    private final ShowDatabase shows;

    public ShowsResource(ShowDatabase shows) {
        this.shows = shows;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public View getShowsDatabase() {
        return new ShowsView(shows.getAllEpisodes());
    }
}
