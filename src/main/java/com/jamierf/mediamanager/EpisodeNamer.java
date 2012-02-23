package com.jamierf.mediamanager;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.jamierf.epdirscanner.EpDirScanner;
import com.jamierf.epdirscanner.Episode;
import com.jamierf.epdirscanner.EpisodeNotFoundException;
import com.jamierf.epguidesparser.EpisodeInfo;
import com.jamierf.epguidesparser.SeriesNotFoundException;

public class EpisodeNamer {

	private static final Pattern FILE_EXT_REGEX = Pattern.compile("\\.([^\\.]+)$");
	private static final Pattern FILENAME_REMOVAL_REGEX = Pattern.compile("[\\|\\\\\\?\\*<\":>\\+\\[\\]/!]");

	public static String getFileExtension(String name) {
		final Matcher matcher = FILE_EXT_REGEX.matcher(name);
		if (!matcher.find())
			return null;

		return matcher.group(1).toLowerCase();
	}

	public static String cleanFilenamePart(String filename) {
		return FILENAME_REMOVAL_REGEX.matcher(filename).replaceAll("");
	}

	private final File mediaDir;
	private final EpDirScanner episodes;

	public EpisodeNamer(File mediaDir, EpDirScanner episodes) {
		this.mediaDir = mediaDir;
		this.episodes = episodes;
	}

	public File getEpisodeFile(String originalName, String title, int season, int episode) throws IOException {
		try {
			final Episode ep = episodes.getOrCreateSeries(title).getEpisode(season, episode);
			if (ep == null)
				throw new IOException(new EpisodeNotFoundException("No such episode"));

			final EpisodeInfo info = ep.getInfo();

			return new File(mediaDir, String.format("%s/Season %s/%s - %s.%s",
				EpisodeNamer.cleanFilenamePart(info.getSeries()),
				StringUtils.leftPad(String.valueOf(info.getSeason()), 2, '0'),
				StringUtils.leftPad(String.valueOf(info.getEpisode()), 2, '0'),
				EpisodeNamer.cleanFilenamePart(info.getTitle()),
				EpisodeNamer.getFileExtension(originalName)
			));
		}
		catch (SeriesNotFoundException e) {
			throw new IOException(e);
		}
	}
}
