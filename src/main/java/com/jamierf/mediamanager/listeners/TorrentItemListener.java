package com.jamierf.mediamanager.listeners;

import com.jamierf.mediamanager.downloader.Downloader;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.ShowDatabase;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.FeedListener;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.rss.RSSItem;
import com.yammer.dropwizard.logging.Log;

import java.io.IOException;
import java.util.Set;

public class TorrentItemListener implements FeedListener<RSSItem> {

    private static final Log LOG = Log.forClass(TorrentItemListener.class);

    private final Set<String> desiredQualities;
    private final ShowDatabase shows;
    private final Downloader torrentDownloader;

    public TorrentItemListener(Set<String> desiredQualities, ShowDatabase shows, Downloader torrentDownloader) {
        this.desiredQualities = desiredQualities;
        this.shows = shows;
        this.torrentDownloader = torrentDownloader;
    }

    @Override
    public void onNewItem(RSSItem item) {
        final Episode.Name name = EpisodeNameParser.parseFilename(item.getTitle());
        if (name == null) {
            if (LOG.isTraceEnabled())
                LOG.trace("Failed to parse episode title: " + item.getTitle());

            return;
        }

        // Check if it is a desired episode
        final Episode episode = shows.getDesiredEpisode(name.getTitle(), name.getSeason(), name.getEpisode());
        if (episode == null) {
            if (LOG.isDebugEnabled())
                LOG.debug("Skipping {} episode s{}e{}, not desired", name.getTitle(), name.getSeason(), name.getEpisode());

            return;
        }

        // Check it is a desired quality
        if (!desiredQualities.contains(name.getQuality())) {
            if (LOG.isDebugEnabled())
                LOG.debug("Skipping torrent {} s{}e{}, undesired quality: {} (desired: {})", name.getTitle(), name.getSeason(), name.getEpisode(), name.getQuality(), desiredQualities);

            return;
        }

        // This is an episode we want!

        if (LOG.isInfoEnabled())
            LOG.info("Downloading torrent: {} s{}e{} ({})", name.getTitle(), name.getSeason(), name.getEpisode(), name.getQuality());

        try {
            // Download the torrent file
            torrentDownloader.download(item.getLink());

            // Mark the episode as pending so we no longer desire it
            episode.setState(State.EXISTS);
        }
        catch (IOException e) {
            LOG.error("Failed to download torrent", e);
        }
    }

    @Override
    public void onException(Throwable cause) {
        LOG.error(cause, "Failed parsing torrent item");
    }
}
