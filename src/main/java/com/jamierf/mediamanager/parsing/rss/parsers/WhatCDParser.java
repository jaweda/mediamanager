package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.parsing.ParserException;
import com.jamierf.mediamanager.parsing.rss.RSSItem;
import com.yammer.dropwizard.client.HttpClientFactory;
import org.apache.commons.lang.StringEscapeUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

public class WhatCDParser extends RSSParser {

	private static final String FEED_URL = "https://ssl.what.cd/feeds.php?feed=torrents_all&user=%d&auth=%s&passkey=%s&authkey=%s";

	public WhatCDParser(HttpClientFactory clientFactory, ParserConfiguration config) throws MalformedURLException, ParserException {
		super (clientFactory, String.format(FEED_URL, config.getInt("userId"), config.getString("authId"), config.getString("passKey"), config.getString("authKey")));
	}

	@Override
	protected RSSItem newItem(String guid, String title, Date date, URL link, String description) throws MalformedURLException, ParseException {
		title = StringEscapeUtils.unescapeHtml(title);
		description = StringEscapeUtils.unescapeHtml(description);

		return super.newItem(guid, title, date, link, description);
	}
}
