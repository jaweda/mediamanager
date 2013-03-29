package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.jamierf.mediamanager.parsing.ParserException;
import com.sun.jersey.api.client.Client;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;

public class SCCParser extends RSSParser {

	private static final String FEED_URL = "https://sceneaccess.eu/rss?feed=dl&passkey=%s";

    private static final String getCategoryFragment(String categories) {
        if (categories == null || categories.isEmpty())
            return "";

        return "&cat=" + categories;
    }

	public SCCParser(Client client, RetryManager retryManager, ParserConfiguration config) throws MalformedURLException, ParserException {
		super (client, retryManager, String.format(FEED_URL, config.getString("passKey")) + SCCParser.getCategoryFragment(config.getString("categories")));
	}

	@Override
	protected Date parseDate(String date) throws ParseException {
		return null; // SCC has no date field for some reason, so we can't convert it
	}
}
