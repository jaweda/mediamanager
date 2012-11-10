package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.parsing.ParserException;
import com.yammer.dropwizard.client.HttpClientFactory;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BitMeParser extends RSSParser {

	private static final String FEED_URL = "http://www.bitme.org/rss.php?feed=dl&passkey=%s";

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

	public BitMeParser(HttpClientFactory clientFactory, ParserConfiguration config) throws ParserException, MalformedURLException {
		super (clientFactory, String.format(FEED_URL, config.getString("passKey")));
	}

	@Override
	protected Date parseDate(String date) throws ParseException {
		return DATE_FORMAT.parse(date);
	}
}
