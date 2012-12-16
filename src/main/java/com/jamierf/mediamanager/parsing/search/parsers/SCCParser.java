package com.jamierf.mediamanager.parsing.search.parsers;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.parsing.search.SearchItem;
import com.jamierf.mediamanager.parsing.search.SearchParser;
import com.yammer.dropwizard.client.HttpClientFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCCParser extends SearchParser {

    private static final String SEARCH_URL = "https://sceneaccess.eu/browse";
    private static final String LINK_URL = "https://sceneaccess.eu/download/%d/%s/%s";

    private static final Pattern URL_ID_REGEX = Pattern.compile("id=(\\d+)", Pattern.CASE_INSENSITIVE);

    private final String passKey;

    public SCCParser(HttpClientFactory clientFactory, ParserConfiguration config) {
        super(clientFactory, SEARCH_URL);

        super.addCookie("uid", config.getInt("uid"));
        super.addCookie("pass", config.getString("pass"));

        passKey = config.getString("passKey");
    }

    private URL createLink(String name, int id) throws MalformedURLException, UnsupportedEncodingException {
        final String filename = String.format("%s.torrent", URLEncoder.encode(name, "UTF-8"));
        return new URL(String.format(LINK_URL, id, passKey, filename));
    }

    @Override
    protected HttpUriRequest buildRequest(String query) throws URISyntaxException {
        final URI url = new URIBuilder(super.getUrl())
                .setParameter("search", query)
                .setParameter("method", "2")
                .build();

        return new HttpGet(url);
    }

    @Override
    protected Set<SearchItem> parse(Reader in) throws Exception {
        // Read in the entire page at once
        final Document doc = Jsoup.parse(CharStreams.toString(in));

        final ImmutableSet.Builder<SearchItem> items = ImmutableSet.builder();

        // Find the table of torrents
        final Elements rows = doc.select("#torrents-table").select("tr");
        for (Element row : rows) {
            final SearchItem item = this.parseItem(row);
            if (item == null)
                continue;

            items.add(item);
        }

        return items.build();
    }

    private SearchItem parseItem(Element e) throws MalformedURLException, UnsupportedEncodingException {
        final Elements fields = e.select("td");
        if (fields.isEmpty())
            return null;

        final Element title = fields.get(1).select("a").first();
        final String name = title.text();

        final Matcher matcher = URL_ID_REGEX.matcher(title.attr("href"));
        if (!matcher.find())
            return null;

        final int id = Integer.parseInt(matcher.group(1));
        final URL link = this.createLink(name, id);

        return new SearchItem(id, name, link);
    }
}
