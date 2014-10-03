package com.jamierf.mediamanager.listeners;

import com.google.common.base.Optional;
import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.downloader.Downloader;
import com.jamierf.mediamanager.filters.QualityFilter;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.Name;
import com.jamierf.mediamanager.models.NameAndQuality;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.DownloadableItem;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.ItemListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DownloadableItemListener implements ItemListener<DownloadableItem> {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadableItemListener.class);

    private final QualityFilter qualityFilter;
    private final ShowDatabase shows;
    private final Downloader torrentDownloader;
    private final EpisodeNameParser episodeNameParser;

    public DownloadableItemListener(QualityFilter qualityFilter, ShowDatabase shows, Downloader torrentDownloader, EpisodeNameParser episodeNameParser) {
        this.qualityFilter = qualityFilter;
        this.shows = shows;
        this.torrentDownloader = torrentDownloader;
        this.episodeNameParser = episodeNameParser;
    }

    private Optional<Episode> getEpisode(Name name) {
        try {
            return shows.get(name);
        } catch (IOException e) {
            LOG.warn("Failed to fetch episode: " + name, e);
            return Optional.absent();
        }
    }

    private void updateEpisode(Episode episode) {
        // Make a copy with the PENDING state
        episode = episode.copyWithState(State.PENDING);

        // Update the database
        try {
            shows.addOrUpdate(episode);
        } catch (IOException e) {
            LOG.error("Error updating episode: " + episode, e);
        }
    }

    @Override
    public synchronized void onNewItem(DownloadableItem item) {
        final NameAndQuality nameAndQuality = episodeNameParser.parseFilename(item.getTitle());
        if (nameAndQuality == null) {
            LOG.trace("Failed to parse episode title: " + item.getTitle());
            return;
        }

        // Check if it is a desired episode
        final Optional<Episode> episode = this.getEpisode(nameAndQuality.getName());
        if (!episode.isPresent() || !episode.get().isDesired()) {
            LOG.trace("Skipping {}, not desired", nameAndQuality);
            return;
        }

        // Check it is a desired quality
        if (!qualityFilter.apply(nameAndQuality)) {
            LOG.trace("Skipping torrent {}, not a desired quality", nameAndQuality);
            return;
        }

        // This is an episode we want!
        LOG.info("Downloading torrent {}", nameAndQuality);

        try {
            // Download the torrent file
            torrentDownloader.download(item.getLink());
        }
        catch (Exception e) {
            LOG.warn("Failed to download torrent", e);
            return;
        }

        try {
            // Mark the episode as pending so we no longer desire it
            this.updateEpisode(episode.get());
        }
        catch (Exception e) {
            LOG.warn("Failed to update episode in database", e);
        }
    }

    @Override
    public void onException(Throwable cause) {
        LOG.warn("Failed parsing downloadable item", cause);
    }
}
