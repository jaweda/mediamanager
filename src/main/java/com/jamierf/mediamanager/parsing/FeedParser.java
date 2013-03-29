package com.jamierf.mediamanager.parsing;

import com.jamierf.mediamanager.io.HttpParser;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class FeedParser<T extends FeedItem> extends HttpParser<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FeedParser.class);

    public FeedParser(Client client, RetryManager retryManager, String url, String method) {
        super(client, retryManager, url, method);
    }

    public Set<T> parse() throws Exception {
        final WebResource.Builder resource = this.buildResource().getRequestBuilder();
        final String content = this.fetchContent(resource);

        final Set<T> results = this.parse(content);

        if (LOG.isDebugEnabled())
            LOG.debug("Parsed {} feed results", results.size());

        return results;
    }
}
