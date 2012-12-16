package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.parsing.ParserException;
import com.yammer.dropwizard.client.HttpClientFactory;

import java.net.MalformedURLException;

public class BitMeTVParser extends RSSParser {

	private static final String FEED_URL = "http://www.bitmetv.org/rss.php?uid=%d&passkey=%s";

	public BitMeTVParser(HttpClientFactory clientFactory, ParserConfiguration config) throws MalformedURLException, ParserException {
		super (clientFactory, String.format(FEED_URL, config.getInt("userId"), config.getString("passKey")));
	}
}
