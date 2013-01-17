package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.jamierf.mediamanager.parsing.ParserException;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.client.JerseyClient;

import java.net.MalformedURLException;

public class BitMeTVParser extends RSSParser {

	private static final String FEED_URL = "http://www.bitmetv.org/rss.php?uid=%d&passkey=%s";

	public BitMeTVParser(JerseyClient client, RetryManager retryManager, ParserConfiguration config) throws MalformedURLException, ParserException {
		super (client, retryManager, String.format(FEED_URL, config.getInt("userId"), config.getString("passKey")));
	}
}
