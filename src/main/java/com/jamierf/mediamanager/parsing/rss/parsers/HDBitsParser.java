package com.jamierf.mediamanager.parsing.rss.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.jamierf.mediamanager.parsing.ParserException;
import com.sun.jersey.api.client.Client;

import java.net.MalformedURLException;

public class HDBitsParser extends RSSParser {

    // category = h264 TV encodes
    private static final String FEED_URL = "http://hdbits.org/rss.php?feed=dl&cats[]=2c1c3&passkey=%s";

    public HDBitsParser(Client client, RetryManager retryManager, ParserConfiguration config) throws MalformedURLException, ParserException {
        super (client, retryManager, String.format(FEED_URL, config.getString("passKey")));
    }
}
