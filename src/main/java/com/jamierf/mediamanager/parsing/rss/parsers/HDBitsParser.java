package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.parsing.ParserException;
import com.yammer.dropwizard.client.HttpClientFactory;

import java.net.MalformedURLException;

public class HDBitsParser extends RSSParser {

	private static final String FEED_URL = "http://hdbits.org/rss.php?feed=dl&passkey=%s";

	public HDBitsParser(HttpClientFactory clientFactory, ParserConfiguration config) throws MalformedURLException, ParserException {
		super (clientFactory, String.format(FEED_URL, config.getString("passKey")));
	}
}
