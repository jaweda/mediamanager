package com.jamierf.mediamanager;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.jamierf.rssfeeder.ParserException;
import com.jamierf.rssfeeder.parsers.RSSParser;
import com.jamierf.rssfeeder.parsers.torrent.BitMeParser;
import com.jamierf.rssfeeder.parsers.torrent.BitMeTVParser;
import com.jamierf.rssfeeder.parsers.torrent.HDBitsParser;
import com.jamierf.rssfeeder.parsers.torrent.SCCParser;

public class Config {

	public static final int DEFAULT_RSS_UPDATE_DELAY = 15;
	public static final int DEFAULT_CACHE_TTL = 24;

	private static String getRequiredString(Configuration config, String key) throws ConfigurationException {
		if (!config.containsKey(key))
			throw new ConfigurationException("Missing configuration option: " + key);

		return config.getString(key);
	}

	private static int getRequiredInt(Configuration config, String key) throws ConfigurationException {
		if (!config.containsKey(key))
			throw new ConfigurationException("Missing configuration option: " + key);

		return config.getInt(key);
	}

	private File directoryRoot;
	private int rssUpdateDelay;
	private final Set<String> desiredQualities;
	private File torrentWatchDir;
	private final Map<String, String> seriesMapping;
	private final Collection<RSSParser> feedParsers;
	private final File cacheDir;
	private final int cacheTTL;

	@SuppressWarnings("unchecked")
	public Config(File file) throws ConfigurationException {
		if (!file.exists())
			throw new ConfigurationException("Missing configuration file: " + file);

		final Configuration config = new PropertiesConfiguration(file);

		directoryRoot = new File(Config.getRequiredString(config, "directoryroot"));
		if (!directoryRoot.isDirectory())
			throw new ConfigurationException("Directory root is not a directory");

		rssUpdateDelay = config.getInt("rssupdatedelay", DEFAULT_RSS_UPDATE_DELAY);

		desiredQualities = new HashSet<String>();
		if (config.containsKey("qualities"))
			desiredQualities.addAll(config.getList("qualities"));

		torrentWatchDir = new File(Config.getRequiredString(config, "watchdir"));
		if (!torrentWatchDir.isDirectory())
			throw new ConfigurationException("Torrent watch directory is not a directory");

		seriesMapping = new HashMap<String, String>();
		// TODO: load mapping

		feedParsers = new LinkedList<RSSParser>();
		try {
			final List<String> feeds = config.getList("feeds");
			for (String feed : feeds) {
				switch (feed.toLowerCase()) {
					case "scc": {
						feedParsers.add(new SCCParser(Config.getRequiredString(config, "feeds.scc.passkey")));
						break;
					}
					case "hdbits": {
						feedParsers.add(new HDBitsParser(Config.getRequiredString(config, "feeds.hdbits.passkey")));
						break;
					}
					case "bitmetv": {
						feedParsers.add(new BitMeTVParser(Config.getRequiredInt(config, "feeds.bitmetv.user"), Config.getRequiredString(config, "feeds.bitmetv.passkey")));
						break;
					}
					case "bitme": {
						feedParsers.add(new BitMeParser(Config.getRequiredString(config, "feeds.bitme.passkey")));
						break;
					}
					default: {
						throw new ConfigurationException("Unrecognised feed: " + feed);
					}
				}
			}
		}
		catch (MalformedURLException | ParserException e) {
			throw new ConfigurationException("Error adding feed parser", e);
		}

		if (config.containsKey("cachedir")) {
			cacheDir = new File(config.getString("cachedir"));
			if (!cacheDir.isDirectory())
				throw new ConfigurationException("Cache directory is not a directory");
		}
		else
			cacheDir = null;

		cacheTTL = config.getInt("cachettl", DEFAULT_CACHE_TTL);
	}

	public File getDirectoryRoot() {
		return directoryRoot;
	}

	public int getRssUpdateDelay() {
		return rssUpdateDelay;
	}

	public boolean isDesiredQuality(String quality) {
		return desiredQualities.isEmpty() || desiredQualities.contains(quality.toLowerCase());
	}

	public File getTorrentWatchDir() {
		return torrentWatchDir;
	}

	public Map<String, String> getSeriesMapping() {
		return seriesMapping;
	}

	public Collection<RSSParser> getFeedParsers() {
		return feedParsers;
	}

	public File getCacheDir() {
		return cacheDir;
	}

	public int getCacheTTL() {
		return cacheTTL;
	}
}
