package com.jamierf.mediamanager;

import com.jamierf.mediamanager.config.MediaConfiguration;
import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.downloader.Downloader;
import com.jamierf.mediamanager.downloader.WatchDirDownloader;
import com.jamierf.mediamanager.handler.GarbageFileHandler;
import com.jamierf.mediamanager.handler.MediaFileHandler;
import com.jamierf.mediamanager.handler.MediaRarFileHandler;
import com.jamierf.mediamanager.io.InsecureHttpClientFactory;
import com.jamierf.mediamanager.listeners.CalendarItemListener;
import com.jamierf.mediamanager.listeners.TorrentItemListener;
import com.jamierf.mediamanager.managers.DownloadDirManager;
import com.jamierf.mediamanager.managers.FeedManager;
import com.jamierf.mediamanager.models.ShowDatabase;
import com.jamierf.mediamanager.parsing.FeedParser;
import com.jamierf.mediamanager.parsing.ical.CalendarItem;
import com.jamierf.mediamanager.parsing.ical.parsers.CalendarParser;
import com.jamierf.mediamanager.parsing.rss.RSSItem;
import com.jamierf.mediamanager.parsing.rss.parsers.RSSParser;
import com.jamierf.mediamanager.resources.ShowsResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.config.Environment;

import java.util.Map;

public class MediaManager extends Service<MediaConfiguration> {

	public static void main(String[] args) throws Exception {
        new MediaManager().run(args);
	}

    @Override
    protected void initialize(MediaConfiguration config, Environment env) throws Exception {
        final HttpClientFactory clientFactory = new InsecureHttpClientFactory(config.getHttpClientConfiguration());

        final Downloader torrentDownloader = new WatchDirDownloader(clientFactory, config.getTorrentWatchDir());
        env.manage(torrentDownloader);

        final ShowDatabase shows = new ShowDatabase(config.getBeforeAirDuration(), config.getAfterAirDuration());
        env.manage(shows);

        // Add shows endpoint
        env.addResource(new ShowsResource(shows));

        final FeedManager<CalendarItem> calendarFeed = new FeedManager<CalendarItem>(config.getCalendarUpdateDelay());
        final CalendarItemListener calendarListener = new CalendarItemListener(shows);

        calendarFeed.addListener(calendarListener);
        env.manage(calendarFeed);

        // Load in all configured calendar parsers
        final Map<String, ParserConfiguration> icalParsers = config.getCalendarParsers();
        for (String name : icalParsers.keySet()) {
            final ParserConfiguration parserConfig = icalParsers.get(name);
            final CalendarParser parser = FeedParser.getInstance(CalendarParser.class, name, clientFactory, parserConfig);

            calendarFeed.addParser(parser);
        }

        final FeedManager<RSSItem> torrentFeed = new FeedManager<RSSItem>(config.getTorrentUpdateDelay());
        final TorrentItemListener torrentListener = new TorrentItemListener(config.getDesiredQualities(), shows, torrentDownloader);

        torrentFeed.addListener(torrentListener);
        env.manage(torrentFeed);

		// Load in all configured torrent parsers
        final Map<String, ParserConfiguration> rssParsers = config.getRssParsers();
        for (String name : rssParsers.keySet()) {
            final ParserConfiguration parserConfig = rssParsers.get(name);
            final RSSParser parser = FeedParser.getInstance(RSSParser.class, name, clientFactory, parserConfig);

            torrentFeed.addParser(parser);
        }

		final DownloadDirManager downloadDirManager = new DownloadDirManager(config.getTorrentDownloadDir());
        env.manage(downloadDirManager);

		downloadDirManager.addFileTypeHandler(new MediaRarFileHandler(config.getMediaDir(), config.isOverwriteFiles(), config.isDeleteArchives()));
		downloadDirManager.addFileTypeHandler(new MediaFileHandler(config.getMediaDir(), config.isMoveFiles(), config.isOverwriteFiles()));
    }
}
