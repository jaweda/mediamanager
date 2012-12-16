package com.jamierf.mediamanager.resources;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.managers.BackfillManager;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.yammer.dropwizard.logging.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/backfill")
@Produces(MediaType.APPLICATION_JSON)
public class BackfillResource {

    private static final Log LOG = Log.forClass(BackfillResource.class);

    private final ShowDatabase shows;
    private final BackfillManager manager;

    public BackfillResource(ShowDatabase shows, BackfillManager manager) {
        this.shows = shows;
        this.manager = manager;
    }

    @PUT
    public Response markEpisode(@QueryParam("episode") String query) {
        final Episode.Name name = EpisodeNameParser.parseFilename(query);
        if (name == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to parse requested episode name").build();

        if (LOG.isInfoEnabled())
            LOG.info("Marking episode {} for backfill", name);

        final Episode episode = new Episode(name, null, null, State.DESIRED);
        if (!shows.addOrUpdate(episode)) // If already in DB overwrite since we specifically requested
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        return Response.status(Response.Status.OK).entity(String.format("Marked %s for backfill", name)).build();
    }

    @POST
    public Response schedule() {
        manager.schedule();

        if (LOG.isInfoEnabled())
            LOG.info("Scheduled a new backfill");

        return Response.status(Response.Status.OK).entity("Scheduled a new backfill").build();
    }
}
