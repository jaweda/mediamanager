package com.jamierf.mediamanager.parsing;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.logging.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.reflections.Reflections;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Set;

public abstract class FeedParser<T extends FeedItem> {

    private static final Log LOG = Log.forClass(FeedParser.class);

    public static <T extends FeedParser<? extends FeedItem>> T getInstance(Class<T> base, String name, HttpClientFactory clientFactory, ParserConfiguration config) throws ClassNotFoundException {
        // Lowercase the requested name
        name = name.toLowerCase();

        // Ensure we always pass a configuration map
        if (config == null)
            config = new ParserConfiguration();

        final Set<Class<? extends T>> classes = new Reflections(base).getSubTypesOf(base);
        for (Class<? extends T> clazz : classes) {
            String parserName = clazz.getSimpleName();
            if (parserName.endsWith("Parser"))
                parserName = parserName.substring(0, parserName.length() - 6);

            // If this isn't the parser we are looking for
            if (!name.equals(parserName.toLowerCase()))
                continue;

            try {
                final Constructor constructor = clazz.getDeclaredConstructor(HttpClientFactory.class, ParserConfiguration.class);

                if (LOG.isInfoEnabled())
                    LOG.info("Creating new feed parser for {}", parserName);

                return base.cast(constructor.newInstance(clientFactory, config));
            }
            catch (NoSuchMethodException e) {
                LOG.error(e, "Failed to load constructor of {} parser", parserName);
            }
            catch (IllegalAccessException e) {
                LOG.error(e, "Failed to access non-public constructor of {} parser", parserName);
            }
            catch (Exception e) {
                LOG.error(e, "Unknown error invoking constructor of {} parser", parserName);
            }
        }

        return null;
    }

    private final HttpClientFactory clientFactory;
    private final String url;

    public FeedParser(HttpClientFactory clientFactory, String url) {
        this.clientFactory = clientFactory;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    protected abstract Set<T> parse(InputStream in) throws Exception;

    public Set<T> parse() throws Exception {
        final HttpClient client = clientFactory.build();

        final HttpResponse response = client.execute(new HttpGet(url));
        final InputStream in = response.getEntity().getContent();

        try {
            if (LOG.isTraceEnabled())
                LOG.trace("Fetched feed from {}", url);

            final Set<T> items = this.parse(in);

            if (LOG.isDebugEnabled())
                LOG.debug("Parsed {} items from {}", items.size(), url);

            return items;
        }
        finally {
            in.close();
        }
    }
}
