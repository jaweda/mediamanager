package com.jamierf.mediamanager;

import com.jamierf.mediamanager.config.*;
import com.jamierf.mediamanager.db.BDBShowDatabase;
import com.jamierf.mediamanager.downloader.Downloader;
import com.jamierf.mediamanager.downloader.WatchDirDownloader;
import com.jamierf.mediamanager.handler.MediaFileHandler;
import com.jamierf.mediamanager.handler.MediaRarFileHandler;
import com.jamierf.mediamanager.io.InsecureHttpClientFactory;
import com.jamierf.mediamanager.listeners.CalendarItemListener;
import com.jamierf.mediamanager.listeners.TorrentItemListener;
import com.jamierf.mediamanager.managers.DownloadDirManager;
import com.jamierf.mediamanager.managers.FeedManager;
import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.parsing.FeedParser;
import com.jamierf.mediamanager.parsing.ical.CalendarItem;
import com.jamierf.mediamanager.parsing.ical.parsers.CalendarParser;
import com.jamierf.mediamanager.parsing.rss.RSSItem;
import com.jamierf.mediamanager.parsing.rss.parsers.RSSParser;
import com.jamierf.mediamanager.resources.ShowsResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.config.Environment;

import java.io.IOException;
import java.util.Map;

public class MediaManager extends Service<MediaManagerConfiguration> {

	public static void main(String[] args) throws Exception {
        new MediaManager().run(args);
	}

    private static ShowDatabase buildShowDatabase(DatabaseConfiguration config) {
        return new BDBShowDatabase(config);
    }

    private static FeedManager<CalendarItem> buildCalendarFeedManager(CalendarConfiguration config, ShowDatabase shows, HttpClientFactory clientFactory) throws ClassNotFoundException {
        final FeedManager<CalendarItem> calendarFeed = new FeedManager<CalendarItem>(config.getUpdateDelay());
        final CalendarItemListener calendarListener = new CalendarItemListener(shows, config.getBeforeAirDuration(), config.getAfterAirDuration());

        calendarFeed.addListener(calendarListener);

        // Load in all configured calendar parsers
        final Map<String, ParserConfiguration> icalParsers = config.getParsers();
        for (String name : icalParsers.keySet()) {
            final ParserConfiguration parserConfig = icalParsers.get(name);
            final CalendarParser parser = FeedParser.getInstance(CalendarParser.class, name, clientFactory, parserConfig);

            calendarFeed.addParser(parser);
        }

        return calendarFeed;
    }

    private static Downloader buildTorrentFileManager(FileConfiguration config, HttpClientFactory clientFactory) {
        return new WatchDirDownloader(clientFactory, config);
    }

    private static FeedManager<RSSItem> buildTorrentFeedManager(TorrentConfiguration config, ShowDatabase shows, Downloader downloader, HttpClientFactory clientFactory) throws ClassNotFoundException {
        final FeedManager<RSSItem> torrentFeed = new FeedManager<RSSItem>(config.getUpdateDelay());
        final TorrentItemListener torrentListener = new TorrentItemListener(config.getQualities(), shows, downloader);

        torrentFeed.addListener(torrentListener);

        // Load in all configured torrent parsers
        final Map<String, ParserConfiguration> rssParsers = config.getParsers();
        for (String name : rssParsers.keySet()) {
            final ParserConfiguration parserConfig = rssParsers.get(name);
            final RSSParser parser = FeedParser.getInstance(RSSParser.class, name, clientFactory, parserConfig);

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

    @Override
    protected void initialize(MediaManagerConfiguration config, Environment env) throws Exception {
        final HttpClientFactory clientFactory = new InsecureHttpClientFactory(config.getHttpClientConfiguration());

        // Initialise the shows database - this stores what episodes we should be watching for
        final ShowDatabase shows = MediaManager.buildShowDatabase(config.getDatabaseConfiguration());
        env.manage(shows);

        // Initialise the calendar feed manager - this periodically parses the known calendar feeds to look for new episodes we want to watch for
        final FeedManager<CalendarItem> calendarFeedManager = MediaManager.buildCalendarFeedManager(config.getCalendarConfiguration(), shows, clientFactory);
        env.manage(calendarFeedManager);

        // Initialise the torrent file manager - this is responsible for taking a torrent file URL and downloading the torrent contents
        final Downloader torrentFileManager = MediaManager.buildTorrentFileManager(config.getFileConfiguration(), clientFactory);
        env.manage(torrentFileManager);

        // Initialise the torrent feed manager - this periodically parses the known torrent RSS feeds to look for new episodes we are watching for
        final FeedManager<RSSItem> torrentFeedManager = MediaManager.buildTorrentFeedManager(config.getTorrentConfiguration(), shows, torrentFileManager, clientFactory);
        env.manage(torrentFeedManager);

        // Initialise the download dir manager - this listens for new files in the download directory and moves the wanted ones to a specified directory
		final DownloadDirManager downloadDirManager = MediaManager.buildDownloadDirManager(config.getFileConfiguration());
        env.manage(downloadDirManager);

        // Add API endpoints
        env.addResource(new ShowsResource(shows));
    }
}
