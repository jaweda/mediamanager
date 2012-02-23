package com.jamierf.mediamanager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.jamierf.epdirscanner.EpDirScanner;
import com.jamierf.epdirscanner.Episode;
import com.jamierf.epdirscanner.Episode.State;
import com.jamierf.epdirscanner.FilenameParser;
import com.jamierf.epdirscanner.Season;
import com.jamierf.epdirscanner.Series;
import com.jamierf.epguidesparser.EpisodeInfo;
import com.jamierf.mediamanager.downloader.Downloader;
import com.jamierf.mediamanager.downloader.WatchDirDownloader;
import com.jamierf.mediamanager.handler.MediaFileHandler;
import com.jamierf.mediamanager.handler.RarFileHandler;
import com.jamierf.rssfeeder.FeedListener;
import com.jamierf.rssfeeder.Feeder;
import com.jamierf.rssfeeder.RSSItem;
import com.jamierf.rssfeeder.parsers.RSSParser;

public class MediaManager {

	private static final Logger logger = LoggerFactory.getLogger(MediaManager.class);

	private static final String DEFAULT_CONFIG_FILE = "config.ini";

	private static final Options options = new Options();

	static {
		options.addOption("h", false, "Print this help message");
		options.addOption("c", true, "Path to configuration file");
	}

	public static void main(String[] args) throws SAXException, IOException, ConfigurationException, ParseException {
		final CommandLineParser parser = new GnuParser();
		final CommandLine params = parser.parse(options, args);

		if (params.hasOption("h")) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("MediaManager", options);
			return;
		}

		final File configFile;
		if (params.hasOption("c"))
			configFile = new File(params.getOptionValue("c"));
		else
			configFile = new File(DEFAULT_CONFIG_FILE);

		final Config config = new Config(configFile);
		final MediaManager manager = new MediaManager(config);
		manager.start();
	}

	private final Config config;
	private final File directoryRoot;
	private final Feeder feeder;
	private final EpDirScanner epScanner;
	private final Downloader torrentDownloader;
	private final DLManager downloadManager;

	public MediaManager(Config config) throws SAXException, IOException {
		this.config = config;

		feeder = new Feeder();

		// Load in all configured feed parsers
		for (RSSParser parser : config.getFeedParsers())
			feeder.addParser(parser);

		directoryRoot = config.getDirectoryRoot();
		epScanner = new EpDirScanner(directoryRoot, config.getSeriesMapping(), config.getCacheDir(), config.getCacheTTL());
		torrentDownloader = new WatchDirDownloader(config.getTorrentWatchDir());

		downloadManager = new DLManager(config.getTorrentDownloadDir());

		final EpisodeNamer namer = new EpisodeNamer(directoryRoot, epScanner);

		downloadManager.addFileTypeHandler(new RarFileHandler(namer, config.isOverwriteFiles()));
		downloadManager.addFileTypeHandler(new MediaFileHandler(namer, config.isMoveFiles(), config.isOverwriteFiles()));
	}

	public void start() {
		epScanner.start();
		downloadManager.start();

		feeder.addListener(new FeedListener() {
			@Override
			public void onNewItem(RSSItem item) {
				processRSSItem(item);
			}

			@Override
			public void onException(Throwable cause) {
				if (logger.isErrorEnabled())
					logger.error("Error parsing RSS feed", cause);
			}
		});
		feeder.start(config.getRssUpdateDelay());
	}

	public Collection<EpisodeInfo> getMissingEpisodes() {
		final Collection<EpisodeInfo> missing = new LinkedList<EpisodeInfo>();

		for (Series series : epScanner.getSeries()) {
			for (Season season : series.getSeasons()) {
				for (Episode episode : season.getEpisodes()) {
					if (episode.getFile() != null)
						continue;

					missing.add(episode.getInfo());
				}
			}
		}

		return missing;
	}

	private void processRSSItem(RSSItem item) {
		final FilenameParser.Parts parts = FilenameParser.parse(item.getTitle());
		if (parts == null) {
			if (logger.isTraceEnabled())
				logger.trace("Failed to parse episode title: " + item.getTitle());

			return;
		}

		// Check if it is a recognised series
		final Series series = epScanner.getSeries(parts.getTitle());
		if (series == null) {
			if (logger.isTraceEnabled())
				logger.trace("Skipping torrent from unknown series: {}", parts.getTitle());

			return;
		}

		// Check it is a recognised episode
		final Episode ep = series.getEpisode(parts.getSeason(), parts.getEpisode());
		if (ep == null) {
			if (logger.isWarnEnabled())
				logger.warn("Skipping torrent, recognised series but unrecognised episode: {} s{}e{}", new Object[]{ parts.getTitle(), parts.getSeason(), parts.getEpisode() });

			return;
		}

		// Check if we already have the episode
		if (ep.getState() != State.WANTED) {
			if (logger.isTraceEnabled())
				logger.trace("Skipping torrent, existing/pending episode: {} s{}e{}", new Object[]{ parts.getTitle(), parts.getSeason(), parts.getEpisode() });

			return;
		}

		// Check it is a desired quality
		if (!config.isDesiredQuality(parts.getQuality())) {
			if (logger.isTraceEnabled())
				logger.trace("Skipping torrent, undesired quality: {}", parts.getQuality());

			return;
		}

		// This is an episode we want!

		if (logger.isDebugEnabled())
			logger.debug("Downloading torrent: {} s{}e{} ({})", new Object[]{ parts.getTitle(), parts.getSeason(), parts.getEpisode(), parts.getQuality() });

		try {
			// Download the torrent file
			torrentDownloader.download(item.getLink());

			// Mark this as pending so we ignore any future torrents
			ep.setPending();
		}
		catch (IOException e) {
			if (logger.isErrorEnabled())
				logger.error("Failed to download torrent", e);
		}
	}
}
