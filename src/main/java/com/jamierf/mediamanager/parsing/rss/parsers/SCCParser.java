package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.parsing.ParserException;
import com.jamierf.mediamanager.parsing.rss.parsers.RSSParser;
import com.yammer.dropwizard.client.HttpClientFactory;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class SCCParser extends RSSParser {

	private static final String FEED_URL = "https://sceneaccess.eu/rss?feed=dl&passkey=%s";

    private static final String getCategoryFragment(String categories) {
        if (categories == null || categories.isEmpty())
            return "";

        return "&cat=" + categories;
    }

	public SCCParser(HttpClientFactory clientFactory, ParserConfiguration config) throws MalformedURLException, ParserException {
		super (clientFactory, String.format(FEED_URL, config.getString("passKey")) + SCCParser.getCategoryFragment(config.getString("categories")));
	}

	@Override
	protected Date parseDate(String date) throws ParseException {
		return null; // SCC has no date field for some reason, so we can't convert it
	}
}
