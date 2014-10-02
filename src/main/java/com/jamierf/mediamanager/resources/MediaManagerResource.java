package com.jamierf.mediamanager.resources;

import com.jamierf.mediamanager.views.HomeView;
import io.dropwizard.views.View;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class MediaManagerResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public View index() {
        return new HomeView();
    }
}

