package com.jamierf.mediamanager.parsing.ical.parsers;

import com.google.common.collect.ImmutableSet;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.jamierf.mediamanager.parsing.FeedParser;
import com.jamierf.mediamanager.parsing.ical.CalendarItem;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.client.JerseyClient;
import com.yammer.dropwizard.logging.Log;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import javax.ws.rs.HttpMethod;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class CalendarParser extends FeedParser<CalendarItem> {

    private static final Log LOG = Log.forClass(CalendarParser.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    private static String getNodeText(Component component, String field, String defaultValue) {
        final Property property = component.getProperty(field);
        if (property == null)
            return defaultValue;

        return property.getValue().trim();
    }

    private final CalendarBuilder builder;

    public CalendarParser(JerseyClient client, RetryManager retryManager, String url) {
        super(client, retryManager, url, HttpMethod.GET);

        builder = new CalendarBuilder();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Set<CalendarItem> parse(String content) throws Exception {
        final Calendar calendar = builder.build(new StringReader(content));

        final ImmutableSet.Builder<CalendarItem> items = ImmutableSet.builder();
        for (Component event : (List<Component>) calendar.getComponents()) {
            try {
                final CalendarItem item = this.parseItem(event);
                if (item == null)
                    continue;

                items.add(item);
            }
            catch (ParseException e) {
                LOG.warn(e, "Failed to parse item start/end date");
            }
        }

        return items.build();
    }

    protected CalendarItem parseItem(Component component) throws ParseException {
        final String uid = CalendarParser.getNodeText(component, "uid", null);
        final String summary = CalendarParser.getNodeText(component, "summary", "");
        final String description = CalendarParser.getNodeText(component, "description", "");

        final Date start = this.parseDate(CalendarParser.getNodeText(component, "dtstart", ""));
        final Date end = this.parseDate(CalendarParser.getNodeText(component, "dtend", ""));

        return this.newItem(uid, summary, description, start, end);
    }

    protected Date parseDate(String date) throws ParseException {
        return DATE_FORMAT.parse(date);
    }

    protected CalendarItem newItem(String uid, String summary, String description, Date start, Date end) {
        return new CalendarItem(uid, summary, description, start, end);
    }
}
