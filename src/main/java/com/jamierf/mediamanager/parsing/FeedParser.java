package com.jamierf.mediamanager.parsing;

import com.jamierf.mediamanager.io.HttpParser;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.logging.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.util.Set;

public abstract class FeedParser<T extends FeedItem> extends HttpParser<T> {

    private static final Log LOG = Log.forClass(FeedParser.class);

    public FeedParser(HttpClientFactory clientFactory, String url) {
        super(clientFactory, url);
    }

    public Set<T> parse() throws Exception {
        final HttpClient client = this.buildClient();
        final HttpContext context = this.buildContext();
        final HttpUriRequest request = this.buildRequest();

        final Set<T> results = this.parse(client, context, request);

        if (LOG.isDebugEnabled())
            LOG.debug("Parsed {} feed results from {}", results.size(), request.getURI());

        return results;
    }
}
