package com.jamierf.mediamanager.parsing.search;

import com.jamierf.mediamanager.io.HttpParser;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class SearchParser extends HttpParser<SearchItem> {

    private static final Logger LOG = LoggerFactory.getLogger(SearchParser.class);

    public SearchParser(Client client, RetryManager retryManager, String url, String method) {
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
