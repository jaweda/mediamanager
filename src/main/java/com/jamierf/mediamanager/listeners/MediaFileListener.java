package com.jamierf.mediamanager.listeners;

import com.google.common.io.Files;
import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.ItemListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MediaFileListener implements ItemListener<File> {

    private static final Logger LOG = LoggerFactory.getLogger(MediaFileListener.class);

    private static String getEpisodePath(Episode.Name name, String originalPath) {
        final StringBuilder builder = new StringBuilder();

        builder.append(name.getTitle());
        builder.append(File.separator);

        // If we have a season then create a sub folder
        final int season = name.getSeason();
        if (season > 0) {
            builder.append(String.format("Season %02d", name.getSeason()));
            builder.append(File.separator);
        }

        builder.append(originalPath);

        return builder.toString();
    }

    private final ShowDatabase shows;
    private final File destDir;
    private final EpisodeNameParser episodeNameParser;

    public MediaFileListener(ShowDatabase shows, File destDir, EpisodeNameParser episodeNameParser) {
        this.shows = shows;
        this.destDir = destDir;
        this.episodeNameParser = episodeNameParser;

        LOG.info("Using destination dir: {}", destDir.getAbsolutePath());
        if (!destDir.isDirectory()) {
            if (LOG.isDebugEnabled())
                LOG.debug("File destination directory '{}' doesn't exist, creating", destDir);

            destDir.mkdirs();
        }
    }

    private Episode getEpisode(String filename) throws Exception {
        final Episode.Name name = episodeNameParser.parseFilename(filename);
        // We cannot parse this name, so we cannot do anything with it...
        if (name == null)
            return null;

        final Episode episode = shows.get(name);
        // The episode already exists, make a copy and mark it as existing
        if (episode != null)
            return episode.copyWithState(State.EXISTS);

        return new Episode(name, State.EXISTS);
    }

    @Override
    public void onNewItem(File item) {
        // Default to just using the original filename
        String path = item.getName();

        try {
            final Episode episode = this.getEpisode(item.getName());
            if (episode == null) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Failed to parse unrecognised episode name: {}", item.getName());

                return;
            }

            if (LOG.isDebugEnabled())
                LOG.debug("Marking new media episode {} as existing", episode.getName());

            // Update to the path to insert the episode to the correct folder
            path = MediaFileListener.getEpisodePath(episode.getName(), path);

            shows.addOrUpdate(episode);
        }
        catch (Exception e) {
            LOG.warn("Failed to mark new media file as existing", e);
        }
        finally {
            final File destFile = new File(destDir.getAbsolutePath() + File.separator + path);
            try {
                // Ensure the parent directory exists
                final File directory = destFile.getParentFile();
                directory.mkdirs();

                if (LOG.isDebugEnabled())
                    LOG.debug("Renaming {} to {}", item.getAbsolutePath(), destFile.getAbsolutePath());

                Files.move(item, destFile);
            }
            catch (IOException e) {
                LOG.error("Error moving temp file to " + destFile, e);
            }
        }
    }

    @Override
    public void onException(Throwable cause) {
        LOG.warn("Exception with media file", cause);
    }
}
