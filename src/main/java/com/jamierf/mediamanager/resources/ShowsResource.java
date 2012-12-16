package com.jamierf.mediamanager.resources;

import com.jamierf.mediamanager.db.ShowDatabase;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/shows")
@Produces(MediaType.APPLICATION_JSON)
public class ShowsResource {

    private final ShowDatabase shows;

    public ShowsResource(ShowDatabase shows) {
        this.shows = shows;
    }

    @GET
    public Response getShowsDatabase() {
        return Response.status(Response.Status.OK).entity(shows.getEpisodes()).build();
    }
}
