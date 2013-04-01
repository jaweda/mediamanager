package com.jamierf.mediamanager.parsing.search.parsers;

import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.jamierf.mediamanager.parsing.search.SearchItem;
import com.jamierf.mediamanager.parsing.search.SearchParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.ws.rs.HttpMethod;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HDBitsParser extends SearchParser {

    private static final String SEARCH_URL = "https://hdbits.org/browse.php";
    private static final String LINK_URL = "https://hdbits.org/download.php/%s?id=%d&passkey=%s";

    private static final Pattern URL_ID_REGEX = Pattern.compile("id=(\\d+)", Pattern.CASE_INSENSITIVE);

    private final int uid;
    private final String pass;
    private final String hash;
    private final String passKey;

    public HDBitsParser(Client client, RetryManager retryManager, ParserConfiguration config) {
        this (client, retryManager, config.getInt("uid"), config.getString("pass"), config.getString("hash"), config.getString("passKey"));
    }

    public HDBitsParser(Client client, RetryManager retryManager, int uid, String pass, String hash, String passKey) {
        super(client, retryManager, SEARCH_URL, HttpMethod.GET);

        this.uid = uid;
        this.pass = pass;
        this.hash = hash;
        this.passKey = passKey;
    }

    private URI createLink(String name, int id) throws MalformedURLException, UnsupportedEncodingException {
        final String filename = String.format("%s.torrent", URLEncoder.encode(name, "UTF-8"));
        return URI.create(String.format(LINK_URL, filename, id, passKey));
    }

    @Override
    protected WebResource.Builder buildResource(String query) {
        final String cookie = String.format("uid=%d; pass=%s; hash=%s", uid, pass, hash);

        return super.buildResource()
                .queryParam("search", query)
                .header("Cookie", cookie);
    }

    @Override
    protected Set<SearchItem> parse(String content) throws Exception {
        // Read in the entire page at once
        final Document doc = Jsoup.parse(content);

        final ImmutableSet.Builder<SearchItem> items = ImmutableSet.builder();

        // Find the table of torrents
        final Elements rows = doc.select("#torrent-list").select("tr");
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

        final Element title = fields.get(2).select("a").first();
        final String name = title.text();

        final Matcher matcher = URL_ID_REGEX.matcher(title.attr("href"));
        if (!matcher.find())
            return null;

        final int id = Integer.parseInt(matcher.group(1));
        final URI link = this.createLink(name, id);

        return new SearchItem(id, name, link);
    }
}
