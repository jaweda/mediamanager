package com.jamierf.mediamanager.parsing.search;

import com.jamierf.mediamanager.io.HttpParser;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.logging.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.net.URISyntaxException;
import java.util.Set;

public abstract class SearchParser extends HttpParser<SearchItem> {

    private static final Log LOG = Log.forClass(SearchParser.class);

    public SearchParser(HttpClientFactory clientFactory, String url) {
        super(clientFactory, url);
    }

    protected abstract HttpUriRequest buildRequest(String query) throws URISyntaxException;

    public Set<SearchItem> search(String query) throws Exception {
        final HttpClient client = this.buildClient();
        final HttpContext context = this.buildContext();
        final HttpUriRequest request = this.buildRequest(query);

        final Set<SearchItem> results = this.parse(client, context, request);

        if (LOG.isDebugEnabled())
            LOG.debug("Parsed {} search results from {}", results.size(), request.getURI());

        return results;
    }
}
