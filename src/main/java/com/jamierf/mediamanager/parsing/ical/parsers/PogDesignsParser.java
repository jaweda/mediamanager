package com.jamierf.mediamanager.parsing.ical.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.client.JerseyClient;

public class PogDesignsParser extends CalendarParser {

    private static final String FEED_URL = "http://www.pogdesign.co.uk/cat/download_ics/%s";

    public PogDesignsParser(JerseyClient client, ParserConfiguration config) {
        super(client, String.format(FEED_URL, config.getString("passKey")));
    }
}
