package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.jamierf.mediamanager.parsing.ParserException;
import com.jamierf.mediamanager.parsing.rss.RSSItem;
import com.sun.jersey.api.client.Client;
import org.apache.commons.lang.StringEscapeUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;

public class TCParser extends RSSParser {

	private static final String FEED_URL = "http://tehconnection.eu/feeds.php?feed=torrents_all&user=%d&auth=%s&passkey=%s&authkey=%s";

	public TCParser(Client client, RetryManager retryManager, ParserConfiguration config) throws MalformedURLException, ParserException {
		super (client, retryManager, String.format(FEED_URL, config.getInt("userId"), config.getString("authId"), config.getString("passKey"), config.getString("authKey")));
	}

	@Override
	protected RSSItem newItem(String guid, String title, Date date, URI link, String description) throws MalformedURLException, ParseException {
		title = StringEscapeUtils.unescapeHtml(title);
		description = StringEscapeUtils.unescapeHtml(description);

		return super.newItem(guid, title, date, link, description);
	}
}
