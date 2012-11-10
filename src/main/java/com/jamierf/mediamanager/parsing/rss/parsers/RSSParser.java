package com.jamierf.mediamanager.parsing.rss.parsers;

import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.parsing.FeedParser;
import com.jamierf.mediamanager.parsing.ParserException;
import com.jamierf.mediamanager.parsing.rss.RSSItem;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class RSSParser extends FeedParser<RSSItem> {

	private static final Log LOG = Log.forClass(RSSParser.class);

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

	public RSSParser(HttpClientFactory clientFactory, String url) throws ParserException {
        super (clientFactory, url);

        this.reader = RSSParser.getSAXReader();
	}

    @Override
    protected Set<RSSItem> parse(InputStream in) throws DocumentException {
        final Document doc = reader.read(in);

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
                LOG.warn(e, "Unable to parse item link");
            }
            catch (ParseException e) {
                LOG.warn(e, "Unable to parse item publish date");
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

		return this.newItem(guid == null ? link : guid, title, this.parseDate(date), new URL(link), description);
	}

	protected Date parseDate(String date) throws ParseException {
		return DATE_FORMAT.parse(date);
	}

	protected RSSItem newItem(String guid, String title, Date date, URL link, String description) throws MalformedURLException, ParseException {
		return new RSSItem(guid, title, date, link, description);
	}
}
