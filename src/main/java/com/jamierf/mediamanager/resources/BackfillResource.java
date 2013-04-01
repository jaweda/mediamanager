package com.jamierf.mediamanager.resources;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.managers.BackfillManager;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.views.BackfillForm;
import com.yammer.dropwizard.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/backfill")
public class BackfillResource {

    private static final Logger LOG = LoggerFactory.getLogger(BackfillResource.class);

    private final ShowDatabase shows;
    private final BackfillManager manager;
    private final EpisodeNameParser episodeNameParser;

    public BackfillResource(ShowDatabase shows, BackfillManager manager, EpisodeNameParser episodeNameParser) {
        this.shows = shows;
        this.manager = manager;
        this.episodeNameParser = episodeNameParser;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public View displayForm() {
        return new BackfillForm();
    }

    @POST
    @Path("/enqueue")
    @Produces(MediaType.TEXT_PLAIN)
    public Response enqueue(@FormParam("query") String query) {
        final Episode.Name name = episodeNameParser.parseFilename(query);
        if (name == null)
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to parse requested episode name").build();

        if (LOG.isInfoEnabled())
            LOG.info("Marking episode {} for backfill", name);

        final Episode episode = new Episode(name, State.DESIRED);
        if (!shows.addOrUpdate(episode)) // If already in DB overwrite since we specifically requested
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        return Response.status(Response.Status.OK).entity(String.format("Marked %s for backfill", name)).build();
    }

    @POST
    @Path("/schedule")
    @Produces(MediaType.TEXT_PLAIN)
    public Response schedule() {
        manager.schedule();

        if (LOG.isInfoEnabled())
            LOG.info("Scheduled a new backfill");

        return Response.status(Response.Status.OK).entity("Scheduled a new backfill").build();
    }
}
