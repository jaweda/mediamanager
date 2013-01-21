package com.jamierf.mediamanager.listeners;

import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.State;
import com.jamierf.mediamanager.parsing.EpisodeNameParser;
import com.jamierf.mediamanager.parsing.ItemListener;
import com.yammer.dropwizard.logging.Log;

import java.io.File;

public class MediaFileListener implements ItemListener<File> {

    private static final Log LOG = Log.forClass(MediaFileListener.class);

    private final ShowDatabase shows;

    public MediaFileListener(ShowDatabase shows) {
        this.shows = shows;
    }

    private Episode getEpisode(String filename) throws Exception {
        final Episode.Name name = EpisodeNameParser.parseFilename(filename);
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
        try {
            final Episode episode = this.getEpisode(item.getName());
            if (episode == null) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Failed to parse unrecognised episode name: {}", item.getName());

                return;
            }

            if (LOG.isDebugEnabled())
                LOG.debug("Marking new media episode {} as existing", episode.getName());

            shows.addOrUpdate(episode);
        }
        catch (Exception e) {
            LOG.warn(e, "Failed to mark new media file as existing");
        }
    }

    @Override
    public void onException(Throwable cause) {
        LOG.warn(cause, "Exception with media file");
    }
}
