package com.jamierf.mediamanager.parsing.ical.parsers;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.client.JerseyClient;

public class PogDesignsParser extends CalendarParser {

    private static final String FEED_URL = "http://www.pogdesign.co.uk/cat/download_ics/%s";

    public PogDesignsParser(JerseyClient client, RetryManager retryManager, ParserConfiguration config) {
        super(client, retryManager, String.format(FEED_URL, config.getString("passKey")));
    }
}
