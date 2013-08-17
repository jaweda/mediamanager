package com.jamierf.mediamanager.listeners;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.downloader.Downloader;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.DownloadableItem;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.ItemListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DownloadableItemListener implements ItemListener<DownloadableItem> {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadableItemListener.class);

    private final Set<String> desiredQualities;
    private final ShowDatabase shows;
    private final Downloader torrentDownloader;
    private final EpisodeNameParser episodeNameParser;

    public DownloadableItemListener(Set<String> desiredQualities, ShowDatabase shows, Downloader torrentDownloader, EpisodeNameParser episodeNameParser) {
        this.desiredQualities = desiredQualities;
        this.shows = shows;
        this.torrentDownloader = torrentDownloader;
        this.episodeNameParser = episodeNameParser;
    }

    private Episode getEpisode(Episode.Name name) {
        try {
            return shows.get(name);
        }
        catch (Exception e) {
            LOG.error("Failed to fetch episode from database", e);
            return null;
        }
    }

    private void updateEpisode(Episode episode) {
        // Make a copy with the PENDING state
        episode = episode.copyWithState(State.PENDING);

        // Update the database
        shows.addOrUpdate(episode);
    }

    @Override
    public synchronized void onNewItem(DownloadableItem item) {
        final Episode.Name name = episodeNameParser.parseFilename(item.getTitle());
        if (name == null) {
            if (LOG.isTraceEnabled())
                LOG.trace("Failed to parse episode title: " + item.getTitle());

            return;
        }

        // Check if it is a desired episode
        final Episode episode = this.getEpisode(name);
        if (episode == null || !episode.isDesired()) {
            if (LOG.isTraceEnabled())
                LOG.trace("Skipping {}, not desired", name);

            return;
        }

        // Check it is a desired quality
        if (!desiredQualities.contains(name.getQuality())) {
            if (LOG.isTraceEnabled())
                LOG.trace("Skipping torrent {}, quality {}, desired {}", name, name.getQuality(), desiredQualities);

            return;
        }

        // This is an episode we want!

        if (LOG.isInfoEnabled())
            LOG.info("Downloading torrent {}", name);

        try {
            // Download the torrent file
            torrentDownloader.download(item.getLink());
        }
        catch (Exception e) {
            LOG.error("Failed to download torrent", e);

            // Return so we don't mark this as pending since we failed to download it
            return;
        }

        try {
            // Mark the episode as pending so we no longer desire it
            this.updateEpisode(episode);
        }
        catch (Exception e) {
            LOG.error("Failed to update episode in database", e);
        }
    }

    @Override
    public void onException(Throwable cause) {
        LOG.error("Failed parsing downloadable item", cause);
    }
}
