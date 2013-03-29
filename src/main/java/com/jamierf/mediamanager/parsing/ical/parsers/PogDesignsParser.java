package com.jamierf.mediamanager.parsing.ical.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.sun.jersey.api.client.Client;

public class PogDesignsParser extends CalendarParser {

    private static final String FEED_URL = "http://www.pogdesign.co.uk/cat/download_ics/%s";

    public PogDesignsParser(Client client, RetryManager retryManager, ParserConfiguration config) {
        super(client, retryManager, String.format(FEED_URL, config.getString("passKey")));
    }
}
