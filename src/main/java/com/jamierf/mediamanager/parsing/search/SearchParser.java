package com.jamierf.mediamanager.parsing.search;

import com.jamierf.mediamanager.io.HttpParser;
import com.yammer.dropwizard.client.HttpClientFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public abstract class SearchParser extends HttpParser<SearchItem> {

    public SearchParser(HttpClientFactory clientFactory, String url) {
        super(clientFactory, url);
    }

    protected abstract HttpUriRequest buildRequest(String query) throws URISyntaxException;

    public Set<SearchItem> search(String query) throws Exception {
        final HttpClient client = this.buildClient();
        final HttpContext context = this.buildContext();
        final HttpUriRequest request = this.buildRequest(query);

        return this.parse(client, context, request);
    }
}
