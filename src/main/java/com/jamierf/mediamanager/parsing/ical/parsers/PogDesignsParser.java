package com.jamierf.mediamanager.parsing.ical.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.yammer.dropwizard.client.HttpClientFactory;

public class PogDesignsParser extends CalendarParser {

    private static final String FEED_URL = "http://www.pogdesign.co.uk/cat/download_ics/%s";

    public PogDesignsParser(HttpClientFactory clientFactory, ParserConfiguration config) {
        super(clientFactory, String.format(FEED_URL, config.getString("passKey")));
    }
}
