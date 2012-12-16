package com.jamierf.mediamanager.listeners;

import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.FeedListener;
import com.jamierf.mediamanager.parsing.ical.CalendarItem;
import com.yammer.dropwizard.logging.Log;
import com.yammer.dropwizard.util.Duration;

import java.util.Calendar;

public class CalendarItemListener implements FeedListener<CalendarItem> {

    private static final Log LOG = Log.forClass(CalendarItemListener.class);

    private final ShowDatabase shows;
    private final Duration beforeDuration;
    private final Duration afterDuration;

    public CalendarItemListener(ShowDatabase shows, Duration beforeDuration, Duration afterDuration) {
        this.shows = shows;
        this.beforeDuration = beforeDuration;
        this.afterDuration = afterDuration;
    }

    @Override
    public void onNewItem(CalendarItem item) {
        final Episode.Name name = EpisodeNameParser.parseCalendarSummary(item.getSummary());
        if (name == null) {
            if (LOG.isTraceEnabled())
                LOG.trace("Failed to parse episode title: " + item.getSummary());

            return;
        }

        final Calendar notBefore = Calendar.getInstance();
        notBefore.setTime(item.getEnd());
        notBefore.add(Calendar.MINUTE, -(int) beforeDuration.toMinutes());

        final Calendar notAfter = Calendar.getInstance();
        notAfter.setTime(item.getEnd());
        notAfter.add(Calendar.MINUTE, +(int) afterDuration.toMinutes());

        try {
            // Attempt to add the desired episode
            final Episode episode = new Episode(name, notBefore, notAfter, State.DESIRED);
            if (!shows.addIfNotExists(episode)) {
                if (LOG.isTraceEnabled())
                    LOG.trace("Failed to add episode {} to database (already exists?)", episode);

                return;
            }

            if (LOG.isInfoEnabled())
                LOG.info("Added new desired episode {}", episode);
        }
        catch (Exception e) {
            LOG.error(e, "Failed to insert episode in to database");
        }
    }

    @Override
    public void onException(Throwable cause) {
        LOG.error(cause, "Failed parsing calendar item");
    }
}
