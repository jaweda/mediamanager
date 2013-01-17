package com.jamierf.mediamanager.parsing;

import com.jamierf.mediamanager.io.HttpParser;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.sun.jersey.api.client.WebResource;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.client.JerseyClient;
import com.yammer.dropwizard.logging.Log;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.util.Set;

public abstract class FeedParser<T extends FeedItem> extends HttpParser<T> {

    private static final Log LOG = Log.forClass(FeedParser.class);

    public FeedParser(JerseyClient client, RetryManager retryManager, String url, String method) {
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
