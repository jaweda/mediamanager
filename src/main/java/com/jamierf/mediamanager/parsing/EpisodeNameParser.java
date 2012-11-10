package com.jamierf.mediamanager.parsing;

import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.models.Episode;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpisodeNameParser {

    public static final ImmutableSet<String> EPISODE_QUALITIES = ImmutableSet.of("720p", "1080p", "1080i", "dvdrip", "hdtv", "ws.pdtv", "pdtv", "ws.dsr", "dsr");

    private static final Pattern TITLE_WHITESPACE_REPLACEMENT_REGEX = Pattern.compile("\\W", Pattern.CASE_INSENSITIVE);
	private static final Pattern EPISODE_QUALITY_REGEX = Pattern.compile("(" + StringUtils.join(EPISODE_QUALITIES, "|").replaceAll("\\.", "\\.") + ")", Pattern.CASE_INSENSITIVE);
	private static final Pattern[] EPISODE_TITLE_REGEXS = {
		Pattern.compile("^/(.+)/Season (\\d{1,2})/(?:ep|episode[\\._ \\-])?\\d*?(\\d{1,2})\\b(?: - )?([^/]*)\\.(?:.+)", Pattern.CASE_INSENSITIVE),

		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*\\[s(\\d+)\\]_\\[(?:ep?|x)(\\d+)\\s*"					+ "([^/]*)$", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*[\\._ \\-]\\s*\\[(\\d+)x(\\d+)\\s*"						+ "([^/]*)$", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*[\\._ \\-](\\d+)x(\\d+)\\s*"								+ "([^/]*)$", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*[\\._ \\-]\\s*s(\\d+)[\\.\\-\\s]?(?:ep?|x)(\\d+)\\s*"	+ "([^/]*)$", Pattern.CASE_INSENSITIVE),

//		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*[\\._ \\-]?\\s*(\\d+)(\\d{2})\\s*"						+ "([^/]*)$", Pattern.CASE_INSENSITIVE),
	};

    private static final Pattern CALENDAR_SUMMARY_REGEX = Pattern.compile("^(.+)\\s+(\\d+)x(\\d+) - .*", Pattern.CASE_INSENSITIVE);

	public static Episode.Name parseFilename(String filename) {
		for (Pattern regex : EPISODE_TITLE_REGEXS) {
			final Matcher matcher = regex.matcher(filename);
			if (!matcher.find())
				continue;

			// required parts
			final String title = EpisodeNameParser.parseTitle(matcher.group(1));
			final int season = Integer.parseInt(matcher.group(2));
			final int episode = Integer.parseInt(matcher.group(3));
			final String quality = EpisodeNameParser.parseQuality(matcher.group(4));

			return new Episode.Name(title, season, episode, quality);
		}

		return null;
	}

    public static Episode.Name parseCalendarSummary(String summary) {
        final Matcher matcher = CALENDAR_SUMMARY_REGEX.matcher(summary);
        if (!matcher.find())
            return null;

        final String title = EpisodeNameParser.parseTitle(matcher.group(1));
        final int season = Integer.parseInt(matcher.group(2));
        final int episode = Integer.parseInt(matcher.group(3));

        return new Episode.Name(title, season, episode, null);
    }

    private static String parseTitle(String title) {
        return TITLE_WHITESPACE_REPLACEMENT_REGEX.matcher(title).replaceAll(" ").trim();
    }

	private static String parseQuality(String extra) {
		final Matcher matcher = EPISODE_QUALITY_REGEX.matcher(extra);
		if (!matcher.find())
			return null;

		return matcher.group(1).toLowerCase().trim();
	}

	private EpisodeNameParser() { }
}
