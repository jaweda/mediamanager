package com.jamierf.mediamanager.parsing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.models.Episode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpisodeNameParser {

    public static final ImmutableSet<String> EPISODE_QUALITIES = ImmutableSet.of("720p", "1080p", "1080i", "bdrip", "dvdrip", "hdtv", "ws.pdtv", "pdtv", "ws.dsr", "dsr");

    private static final Pattern SHOW_TITLE_DELIMS_REGEX = Pattern.compile("[\\.\\-_]", Pattern.CASE_INSENSITIVE);
    private static final Pattern SHOW_TITLE_CLEAN_REGEX = Pattern.compile("[^\\w ]", Pattern.CASE_INSENSITIVE);

	private static final Pattern EPISODE_QUALITY_REGEX = Pattern.compile("(" + StringUtils.join(EPISODE_QUALITIES, "|").replaceAll("\\.", "\\.") + ")", Pattern.CASE_INSENSITIVE);
	private static final Pattern[] EPISODE_TITLE_REGEXS = {
		Pattern.compile("^/(.+)/Season (\\d{1,2})/(?:ep|episode[\\._ \\-])?\\d*?(\\d{1,2})\\b(?: - )?([^/]*)\\.(?:.+)", Pattern.CASE_INSENSITIVE),

		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*\\[s(\\d+)\\]_\\[(?:ep?|x)(\\d+)\\s*"					+ "([^/]*)$", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*[\\._ \\-]\\s*\\[(\\d+)x(\\d+)\\s*"						+ "([^/]*)$", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*[\\._ \\-](\\d+)x(\\d+)\\s*"								+ "([^/]*)$", Pattern.CASE_INSENSITIVE),
		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*[\\._ \\-]\\s*s(\\d+)[\\.\\-\\s]?(?:ep?|x)(\\d+)\\s*"	+ "([^/]*)$", Pattern.CASE_INSENSITIVE),

        Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*part(\\d+)\\s*"	                                        + "([^/]*)$", Pattern.CASE_INSENSITIVE),
//		Pattern.compile("^(?:.*[/-])?([^/]+?)"	+ "\\s*[\\._ \\-]?\\s*(\\d+)(\\d{2})\\s*"						+ "([^/]*)$", Pattern.CASE_INSENSITIVE),
	};

    private static final Pattern CALENDAR_SUMMARY_REGEX = Pattern.compile("^(.+)\\s+(\\d+)x(\\d+) - .*", Pattern.CASE_INSENSITIVE);

    protected static String cleanTitle(String title) {
        title = SHOW_TITLE_DELIMS_REGEX.matcher(title).replaceAll(" "); // replace any delims with spaces
        title = SHOW_TITLE_CLEAN_REGEX.matcher(title).replaceAll(""); // remove any non-word characters
        title = WordUtils.capitalizeFully(title); // capitalise
        return title.trim(); // trim
    }

    private final Map<String, String> aliases;

    public EpisodeNameParser(Map<String, String> aliases) {
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            final String key = EpisodeNameParser.cleanTitle(entry.getKey());
            final String value = EpisodeNameParser.cleanTitle(entry.getValue());

            builder.put(key, value);
        }

        this.aliases = builder.build();
    }

    public Episode.Name parseFilename(String filename) {
		for (Pattern regex : EPISODE_TITLE_REGEXS) {
			final Matcher matcher = regex.matcher(filename);
			if (!matcher.find())
				continue;

            int index = 1;

			// required parts
			final String title = this.parseTitle(matcher.group(index++));
			final int season = matcher.groupCount() > 3 ? Integer.parseInt(matcher.group(index++)) : 0;
			final int episode = Integer.parseInt(matcher.group(index++));
			final String quality = this.parseQuality(matcher.group(index++));

			return new Episode.Name(title, season, episode, quality);
		}

		return null;
	}

    public Episode.Name parseCalendarSummary(String summary) {
        final Matcher matcher = CALENDAR_SUMMARY_REGEX.matcher(summary);
        if (!matcher.find())
            return null;

        int index = 1;

        final String title = this.parseTitle(matcher.group(index++));
        final int season = Integer.parseInt(matcher.group(index++));
        final int episode = Integer.parseInt(matcher.group(index++));

        return new Episode.Name(title, season, episode, null);
    }

    private String parseTitle(String title) {
        title = EpisodeNameParser.cleanTitle(title);

        // Check if we have an alias
        if (aliases.containsKey(title))
            title = aliases.get(title);

        return title;
    }

	private String parseQuality(String extra) {
		final Matcher matcher = EPISODE_QUALITY_REGEX.matcher(extra);
		if (!matcher.find())
			return null;

		return matcher.group(1).toLowerCase().trim();
	}
}
