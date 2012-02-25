package com.jamierf.mediamanager.web;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import com.jamierf.epdirscanner.EpDirScanner;
import com.jamierf.epdirscanner.Episode;
import com.jamierf.epdirscanner.LocalEpisode;
import com.jamierf.epdirscanner.Season;
import com.jamierf.epdirscanner.Series;
import com.jamierf.epguidesparser.EpisodeInfo;

public class MediaTreeHandler implements HttpHandler {

	private static String[] splitPath(String path) {
		if (path == null || path.equals("/"))
			return new String[0];

		return path.substring(1).split("/");
	}

	private static String extToMediaClass(String ext) {
		switch (ext) {
			case "mkv": return "media_hd";
			default: return "media_sd";
		}
	}

	private static String createFolderHtml(String path, String title) {
		path = StringEscapeUtils.escapeHtml(path);
		title = StringEscapeUtils.escapeHtml(title);

		return String.format("<li class=\"directory collapsed\"><a href=\"#\" rel=\"%1$s\" title=\" %2$s \">%2$s</a></li>", path, title);
	}

	private static String createFileHtml(String path, String title, String ext) {
		final String fileclass = ext == null ? "missing" : MediaTreeHandler.extToMediaClass(ext);

		path = StringEscapeUtils.escapeHtml(path);
		title = StringEscapeUtils.escapeHtml(title);

		return String.format("<li class=\"%1$s\"><a href=\"#\" rel=\"%2$s\" title=\" %3$s \">%3$s</a></li>", fileclass, path, title);
	}

	private final EpDirScanner epScanner;

	public MediaTreeHandler(EpDirScanner epScanner) {
		this.epScanner = epScanner;
	}

	@Override
	public void handleHttpRequest(HttpRequest req, HttpResponse resp, HttpControl ctrl) throws Exception {
		final String[] parts = MediaTreeHandler.splitPath(req.postParam("dir"));

		final StringBuilder content = new StringBuilder();

		content.append("<ul class=\"jqueryFileTree\" style=\"display:none\">");

		switch (parts.length) {
			// Show a list of series
			case 0: {
				for (Series series : epScanner.getSeries()) {
					final String path = String.format("/%s", series.getKey());
					content.append(MediaTreeHandler.createFolderHtml(path, series.getTitle()));
				}

				break;
			}

			// Show a list of seasons
			case 1: {
				final Series series = epScanner.getSeries(parts[0]);
				if (series == null)
					return; // TODO: Handle

				for (Season season : series.getSeasons()) {
					final String path = String.format("/%s/%d", series.getKey(), season.getSeason());
					content.append(MediaTreeHandler.createFolderHtml(path, "Season " + season.getSeason()));
				}
				break;
			}

			// Show a list of episodes
			case 2: {
				final Series series = epScanner.getSeries(parts[0]);
				if (series == null)
					return; // TODO: Handle

				final Season season = series.getSeason(Integer.parseInt(parts[1]));
				if (season == null)
					return; // TODO: Handle

				for (Episode episode : season.getEpisodes()) {
					final EpisodeInfo info = episode.getInfo();
					final LocalEpisode local = episode.getFile();

					final String path = String.format("/%s/%d/%s", series.getKey(), season.getSeason(), info.getTitle());
					final String title = String.format("%s %s", StringUtils.leftPad(String.valueOf(info.getEpisode()), 2, '0'), info.getTitle());
					final String ext = local == null ? null : local.getFileExt();

					content.append(MediaTreeHandler.createFileHtml(path, title, ext));
				}
			}
		}

		content.append("</ul>");

		resp.content(content.toString()).end();
	}
}
