package com.jamierf.mediamanager.parsing.search;

import com.jamierf.mediamanager.io.HttpParser;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.sun.jersey.api.client.WebResource;
import com.yammer.dropwizard.client.JerseyClient;
import com.yammer.dropwizard.logging.Log;

import java.util.Set;

public abstract class SearchParser extends HttpParser<SearchItem> {

    private static final Log LOG = Log.forClass(SearchParser.class);

    public SearchParser(JerseyClient client, RetryManager retryManager, String url, String method) {
        super(client, retryManager, url, method);
    }

    protected abstract WebResource.Builder buildResource(String query);

    public Set<SearchItem> search(String query) throws Exception {
        final WebResource.Builder resource = this.buildResource(query);
        final String content = this.fetchContent(resource);

        final Set<SearchItem> results = this.parse(content);

        if (LOG.isDebugEnabled())
            LOG.debug("Parsed {} search results", results.size());

        return results;
    }
}
