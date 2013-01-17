package com.jamierf.mediamanager.io;

import com.jamierf.mediamanager.config.ParserConfiguration;
import com.jamierf.mediamanager.io.retry.RetryManager;
import com.sun.jersey.api.client.WebResource;
import com.yammer.dropwizard.client.JerseyClient;
import com.yammer.dropwizard.logging.Log;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class HttpParser<T extends ParsedItem> {

    private static final Log LOG = Log.forClass(HttpParser.class);

    public static <T extends HttpParser<? extends ParsedItem>> T getInstance(Class<T> base, String name, JerseyClient client, RetryManager retryManager, ParserConfiguration config) throws ClassNotFoundException {
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
                final Constructor constructor = clazz.getDeclaredConstructor(JerseyClient.class, RetryManager.class, ParserConfiguration.class);

                if (LOG.isInfoEnabled())
                    LOG.info("Creating new parser for {}", parserName);

                return base.cast(constructor.newInstance(client, retryManager, config));
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

    private final JerseyClient client;
    private final RetryManager retryManager;
    private final URI url;
    private final String method;

    public HttpParser(JerseyClient client, RetryManager retryManager, String url, String method) {
        this (client, retryManager, URI.create(url), method);
    }

    public HttpParser(JerseyClient client, RetryManager retryManager, URI url, String method) {
        this.client = client;
        this.retryManager = retryManager;
        this.url = url;
        this.method = method;
    }

    protected WebResource buildResource() {
        return client.resource(url);
    }

    protected String fetchContent(final WebResource.Builder resource) {
        return retryManager.apply(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return resource.method(method, String.class);
            }
        });
    }

    public URI getUrl() {
        return url;
    }

    protected abstract Set<T> parse(String content) throws Exception;
}
