package com.jamierf.mediamanager.resources;

import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.views.View;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/")
public class MediaManagerResource {

    public static class HomeView extends View {

        public static class Link {
            private final String title;
            private final String link;

            public Link(String title, String link) {
                this.title = title;
                this.link = link;
            }

            public String getTitle() {
                return title;
            }

            public String getLink() {
                return link;
            }
        }

        private static final ImmutableSet<Link> LINKS = ImmutableSet.of(
                new Link("Shows", "/shows"),
                new Link("Backfill", "/backfill")
        );

        public HomeView() {
            super("home.mustache");
        }

        public String getTitle() {
            return "MediaManager";
        }

        public Collection<Link> getLinks() {
            return LINKS;
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public View index() {
        return new HomeView();
    }
}
