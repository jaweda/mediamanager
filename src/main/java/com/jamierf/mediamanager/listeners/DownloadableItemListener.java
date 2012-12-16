package com.jamierf.mediamanager.listeners;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.downloader.Downloader;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.DownloadableItem;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.ItemListener;
import com.yammer.dropwizard.logging.Log;

import java.io.IOException;
import java.util.Set;

public class DownloadableItemListener implements ItemListener<DownloadableItem> {

    private static final Log LOG = Log.forClass(DownloadableItemListener.class);

    private final Set<String> desiredQualities;
    private final ShowDatabase shows;
    private final Downloader torrentDownloader;

    public DownloadableItemListener(Set<String> desiredQualities, ShowDatabase shows, Downloader torrentDownloader) {
        this.desiredQualities = desiredQualities;
        this.shows = shows;
        this.torrentDownloader = torrentDownloader;
    }

    @Override
    public synchronized void onNewItem(DownloadableItem item) {
        final Episode.Name name = EpisodeNameParser.parseFilename(item.getTitle());
        if (name == null) {
            if (LOG.isTraceEnabled())
                LOG.trace("Failed to parse episode title: " + item.getTitle());

            return;
        }

        // Check if it is a desired episode
        final Episode episode;
        try {
            episode = shows.get(name);
        }
        catch (Exception e) {
            LOG.error(e, "Failed to fetch episode from database");
            return;
        }

        if (episode == null || !episode.isDesiredNow()) {
            if (LOG.isTraceEnabled())
                LOG.trace("Skipping {} episode s{}e{}, not desired", name.getTitle(), name.getSeason(), name.getEpisode());

            return;
        }

        // Check it is a desired quality
        if (!desiredQualities.contains(name.getQuality())) {
            if (LOG.isTraceEnabled())
                LOG.trace("Skipping torrent {} s{}e{}, undesired quality: {} (desired: {})", name.getTitle(), name.getSeason(), name.getEpisode(), name.getQuality(), desiredQualities);

            return;
        }

        // This is an episode we want!

        if (LOG.isInfoEnabled())
            LOG.info("Downloading torrent: {} s{}e{} ({})", name.getTitle(), name.getSeason(), name.getEpisode(), name.getQuality());

        try {
            // Download the torrent file
            torrentDownloader.download(item.getLink());
        }
        catch (IOException e) {
            LOG.error(e, "Failed to download torrent");
        }

        try {
            // Mark the episode as pending so we no longer desire it
            shows.addOrUpdate(episode.copyWithState(State.PENDING));
        }
        catch (Exception e) {
            LOG.error(e, "Failed to update episode in database");
        }
    }

    @Override
    public void onException(Throwable cause) {
        LOG.error(cause, "Failed parsing downloadable item");
    }
}
