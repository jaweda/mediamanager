package com.jamierf.mediamanager;

import com.jamierf.mediamanager.config.*;
import com.jamierf.mediamanager.db.BDBShowDatabase;
import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.downloader.Downloader;
import com.jamierf.mediamanager.downloader.WatchDirDownloader;
import com.jamierf.mediamanager.handler.MediaFileHandler;
import com.jamierf.mediamanager.handler.MediaRarFileHandler;
import com.jamierf.mediamanager.healthchecks.DatabaseHealthCheck;
import com.jamierf.mediamanager.healthchecks.ParserHealthcheck;
import com.jamierf.mediamanager.io.StaticAssetForwarder;
import com.jamierf.mediamanager.listeners.CalendarItemListener;
import com.jamierf.mediamanager.listeners.DownloadableItemListener;
import com.jamierf.mediamanager.listeners.DownloadableItemListenerProxy;
import com.jamierf.mediamanager.managers.BackfillManager;
import com.jamierf.mediamanager.managers.DownloadDirManager;
import com.jamierf.mediamanager.managers.FeedManager;
import com.jamierf.mediamanager.parsing.DownloadableItem;
import com.jamierf.mediamanager.parsing.FeedParser;
import com.jamierf.mediamanager.parsing.ItemListener;
import com.jamierf.mediamanager.parsing.ical.CalendarItem;
import com.jamierf.mediamanager.parsing.ical.parsers.CalendarParser;
import com.jamierf.mediamanager.parsing.rss.RSSItem;
import com.jamierf.mediamanager.parsing.rss.parsers.RSSParser;
import com.jamierf.mediamanager.parsing.search.SearchItem;
import com.jamierf.mediamanager.parsing.search.SearchParser;
import com.jamierf.mediamanager.resources.BackfillResource;
import com.jamierf.mediamanager.resources.MediaManagerResource;
import com.jamierf.mediamanager.resources.ShowsResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.bundles.AssetsBundle;
import com.yammer.dropwizard.client.JerseyClient;
import com.yammer.dropwizard.client.JerseyClientFactory;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

import java.io.IOException;
import java.util.Map;

public class MediaManager extends Service<MediaManagerConfiguration> {

	public static void main(String[] args) throws Exception {
        new MediaManager().run(args);
	}

    private static ShowDatabase buildShowDatabase(DatabaseConfiguration config) {
        return new BDBShowDatabase(config);
    }

    private static DownloadableItemListener buildDownloadableListener(TorrentConfiguration config, ShowDatabase shows, Downloader downloader) {
        return new DownloadableItemListener(config.getQualities(), shows, downloader);
    }

    private static BackfillManager buildBackfillManager(TorrentConfiguration config, ShowDatabase shows, ItemListener<DownloadableItem> downloadableItemListener, JerseyClient client) throws ClassNotFoundException {
        final BackfillManager backfill = new BackfillManager(shows, config.getBackfillDelay());
        backfill.addListener(new DownloadableItemListenerProxy<SearchItem>(downloadableItemListener));

        // Load in all configured search parsers
        final Map<String, ParserConfiguration> searchers = config.getSearchers();
        for (String name : searchers.keySet()) {
            final ParserConfiguration parserConfig = searchers.get(name);
            final SearchParser parser = SearchParser.getInstance(SearchParser.class, name, client, parserConfig);

            backfill.addParser(parser);
        }

        return backfill;
    }

    private static FeedManager<CalendarItem> buildCalendarFeedManager(CalendarConfiguration config, ShowDatabase shows, BackfillManager backfillManager, JerseyClient client) throws ClassNotFoundException {
        final FeedManager<CalendarItem> calendarFeed = new FeedManager<CalendarItem>(config.getUpdateDelay());
        final CalendarItemListener calendarListener = new CalendarItemListener(shows, backfillManager, config.getBeforeAirDuration(), config.getAfterAirDuration());

        calendarFeed.addListener(calendarListener);

        // Load in all configured calendar parsers
        final Map<String, ParserConfiguration> icalParsers = config.getParsers();
        for (String name : icalParsers.keySet()) {
            final ParserConfiguration parserConfig = icalParsers.get(name);
            final CalendarParser parser = FeedParser.getInstance(CalendarParser.class, name, client, parserConfig);

            calendarFeed.addParser(parser);
        }

        return calendarFeed;
    }

    private static Downloader buildTorrentFileManager(TorrentConfiguration config, JerseyClient client) {
        return new WatchDirDownloader(client, config.getWatchDir());
    }

    private static FeedManager<RSSItem> buildTorrentFeedManager(TorrentConfiguration config, ItemListener<DownloadableItem> downloadableItemListener, JerseyClient client) throws ClassNotFoundException {
        final FeedManager<RSSItem> torrentFeed = new FeedManager<RSSItem>(config.getUpdateDelay());
        torrentFeed.addListener(new DownloadableItemListenerProxy<RSSItem>(downloadableItemListener));

        // Load in all configured torrent parsers
        final Map<String, ParserConfiguration> feeders = config.getFeeders();
        for (String name : feeders.keySet()) {
            final ParserConfiguration parserConfig = feeders.get(name);
            final RSSParser parser = FeedParser.getInstance(RSSParser.class, name, client, parserConfig);

            torrentFeed.addParser(parser);
        }

        return torrentFeed;
    }

    private static DownloadDirManager buildDownloadDirManager(FileConfiguration config) throws IOException {
        final DownloadDirManager downloadDirManager = new DownloadDirManager(config);

        downloadDirManager.addFileTypeHandler(new MediaRarFileHandler(config.getDestinationDir(), config.isOverwriteFiles(), config.isDeleteArchives()));
        downloadDirManager.addFileTypeHandler(new MediaFileHandler(config.getDestinationDir(), config.isMoveFiles(), config.isOverwriteFiles()));

        return downloadDirManager;
    }

    public MediaManager() {
        // Enable views
        super.addBundle(new ViewBundle());

        // Enable assets
        super.addBundle(new AssetsBundle());
    }

    @Override
    protected void initialize(MediaManagerConfiguration config, Environment env) throws Exception {
        final JerseyClientFactory clientFactory = new JerseyClientFactory(config.getHttpClientConfiguration());

        // Initialise the shows database - this stores what episodes we should be watching for
        final ShowDatabase shows = MediaManager.buildShowDatabase(config.getDatabaseConfiguration());
        env.manage(shows);

        // Initialise the torrent file manager - this is responsible for taking a torrent file URL and downloading the torrent contents
        final Downloader torrentFileManager = MediaManager.buildTorrentFileManager(config.getTorrentConfiguration(), clientFactory.build(env));
        env.manage(torrentFileManager);

        // Initialise the downloadable item listener - this listens for downloadable items and passes them to the torrent file manager
        final ItemListener<DownloadableItem> downloadableListener = MediaManager.buildDownloadableListener(config.getTorrentConfiguration(), shows, torrentFileManager);

        // Initialise the backfill manager - this searches for missing episodes on demand
        final BackfillManager backfillManager = MediaManager.buildBackfillManager(config.getTorrentConfiguration(), shows, downloadableListener, clientFactory.build(env));
        env.manage(backfillManager);

        // Initialise the calendar feed manager - this periodically parses the known calendar feeds to look for new episodes we want to watch for
        final FeedManager<CalendarItem> calendarFeedManager = MediaManager.buildCalendarFeedManager(config.getCalendarConfiguration(), shows, backfillManager, clientFactory.build(env));
        env.manage(calendarFeedManager);

        // Initialise the torrent feed manager - this periodically parses the known torrent RSS feeds to look for new episodes we are watching for
        final FeedManager<RSSItem> torrentFeedManager = MediaManager.buildTorrentFeedManager(config.getTorrentConfiguration(), downloadableListener, clientFactory.build(env));
        env.manage(torrentFeedManager);

        // Initialise the download dir manager - this listens for new files in the download directory and moves the wanted ones to a specified directory
		final DownloadDirManager downloadDirManager = MediaManager.buildDownloadDirManager(config.getFileConfiguration());
        env.manage(downloadDirManager);

        // Add a filter to redirect favicon to the static assets directory
        env.addFilter(new StaticAssetForwarder(), "/favicon.*");

        // Add API endpoints
        env.addResource(new MediaManagerResource());
        env.addResource(new ShowsResource(shows));
        env.addResource(new BackfillResource(shows, backfillManager));

        // Add ping healthchecks for torrents, calendar, and backfill
        env.addHealthCheck(new ParserHealthcheck(config.getHttpClientConfiguration().getConnectionTimeout(), torrentFeedManager, calendarFeedManager, backfillManager));
        env.addHealthCheck(new DatabaseHealthCheck(shows));
    }
}
