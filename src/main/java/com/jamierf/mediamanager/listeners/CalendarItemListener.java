package com.jamierf.mediamanager.listeners;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.managers.BackfillManager;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.Name;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.ItemListener;
import com.jamierf.mediamanager.parsing.ical.CalendarItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarItemListener implements ItemListener<CalendarItem> {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarItemListener.class);

    private final ShowDatabase shows;
    private final BackfillManager backfillManager;
    private final EpisodeNameParser episodeNameParser;

    public CalendarItemListener(ShowDatabase shows, BackfillManager backfillManager, EpisodeNameParser episodeNameParser) {
        this.shows = shows;
        this.backfillManager = backfillManager;
        this.episodeNameParser = episodeNameParser;
    }

    @Override
    public void onNewItem(CalendarItem item) {
        final Name name = episodeNameParser.parseCalendarSummary(item.getSummary());
        if (name == null) {
            if (LOG.isTraceEnabled())
                LOG.trace("Failed to parse episode title: " + item.getSummary());

            return;
        }

        try {
            // Attempt to add the desired episode
            final Episode episode = new Episode(name, State.DESIRED);
            if (!shows.addIfNotExists(episode)) {
                if (LOG.isTraceEnabled())
                    LOG.trace("Failed to add episode {} to database (already exists?)", episode);

                return;
            }

            if (LOG.isInfoEnabled())
                LOG.info("Added new desired episode {}", episode);

            // Schedule backfill now that we know about a new episode
            backfillManager.schedule();
        }
        catch (Exception e) {
            LOG.error("Failed to insert episode in to database", e);
        }
    }

    @Override
    public void onException(Throwable cause) {
        LOG.error("Failed parsing calendar item", cause);
    }
}
