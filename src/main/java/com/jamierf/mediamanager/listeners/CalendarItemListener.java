package com.jamierf.mediamanager.listeners;

import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.ShowDatabase;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.FeedListener;
import com.jamierf.mediamanager.parsing.ical.CalendarItem;
import com.jamierf.mediamanager.parsing.ical.parsers.PogDesignsParser;
import com.yammer.dropwizard.logging.Log;

import java.util.Calendar;

public class CalendarItemListener implements FeedListener<CalendarItem> {

    private static final Log LOG = Log.forClass(CalendarItemListener.class);

    private final ShowDatabase shows;

    public CalendarItemListener(ShowDatabase shows) {
        this.shows = shows;
    }

    @Override
    public void onNewItem(CalendarItem item) {
        final Episode.Name name = EpisodeNameParser.parseCalendarSummary(item.getSummary());
        if (name == null) {
            if (LOG.isTraceEnabled())
                LOG.trace("Failed to parse episode title: " + item.getSummary());

            return;
        }

        // Attempt to add the desired episode
        if (!shows.addDesiredEpisode(name.getTitle(), name.getSeason(), name.getEpisode(), item.getEnd()))
            return;

        if (LOG.isInfoEnabled())
            LOG.info("Added new desired episode {} s{}e{}", name.getTitle(), name.getSeason(), name.getEpisode());
    }

    @Override
    public void onException(Throwable cause) {
        LOG.error(cause, "Failed parsing calendar item");
    }
}
