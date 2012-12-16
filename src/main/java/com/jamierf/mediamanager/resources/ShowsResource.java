package com.jamierf.mediamanager.resources;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.models.Episode;
import com.yammer.dropwizard.views.View;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/shows")
public class ShowsResource {

    public static class ShowsView extends View {

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
