package com.jamierf.mediamanager.views;

import com.google.common.collect.ImmutableSet;
import io.dropwizard.views.View;

import java.util.Collection;

public class HomeView extends View {

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
