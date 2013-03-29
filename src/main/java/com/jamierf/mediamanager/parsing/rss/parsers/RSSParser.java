package com.jamierf.mediamanager.parsing.rss.parsers;

import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.jamierf.mediamanager.parsing.FeedParser;
import com.jamierf.mediamanager.parsing.ParserException;
import com.jamierf.mediamanager.parsing.rss.RSSItem;
import com.sun.jersey.api.client.Client;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.ws.rs.HttpMethod;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class RSSParser extends FeedParser<RSSItem> {

	private static final Logger LOG = LoggerFactory.getLogger(RSSParser.class);

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

	private static SAXReader getSAXReader() {
		try {
			return new SAXReader(XMLReaderFactory.createXMLReader());
		}
		catch (SAXException e) {
			throw new ParserException(e);
		}
	}

	private static String getNodeText(Element e, String path, String defaultValue) {
		final Node node = e.selectSingleNode(path);
		if (node == null)
			return defaultValue;

		return node.getText().trim();
	}

	private final SAXReader reader;

	public RSSParser(Client client, RetryManager retryManager, String url) throws ParserException {
        super (client, retryManager, url, HttpMethod.GET);

        this.reader = RSSParser.getSAXReader();
	}

    @Override
    protected Set<RSSItem> parse(String content) throws DocumentException, UnsupportedEncodingException {
        final Document doc = reader.read(new StringReader(content));

        @SuppressWarnings("unchecked")
        final List<Element> elements = doc.selectNodes("//rss/channel/item");

        final ImmutableSet.Builder<RSSItem> items = ImmutableSet.builder();
        for (Element element : elements) {
            try {
                final RSSItem item = this.parseItem(element);
                if (item == null)
                    continue;

                items.add(item);
            }
            catch (MalformedURLException e) {
                LOG.warn("Unable to parse item link", e);
            }
            catch (ParseException e) {
                LOG.warn("Unable to parse item publish date", e);
            }
        }

        return items.build();
    }

	protected RSSItem parseItem(Element e) throws DocumentException, MalformedURLException, ParseException {
		final String guid = RSSParser.getNodeText(e, "guid", null);
		final String title = RSSParser.getNodeText(e, "title", "");
		final String date = RSSParser.getNodeText(e, "pubDate", "");
		final String link = RSSParser.getNodeText(e, "link", "");
		final String description = RSSParser.getNodeText(e, "description", "");

		return this.newItem(guid == null ? link : guid, title, this.parseDate(date), URI.create(link), description);
	}

	protected Date parseDate(String date) throws ParseException {
		return DATE_FORMAT.parse(date);
	}

	protected RSSItem newItem(String guid, String title, Date date, URI link, String description) throws MalformedURLException, ParseException {
		return new RSSItem(guid, title, date, link, description);
	}
}
