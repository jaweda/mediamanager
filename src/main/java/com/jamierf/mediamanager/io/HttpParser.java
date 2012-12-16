package com.jamierf.mediamanager.io;

import com.google.common.collect.Lists;
import com.jamierf.mediamanager.config.ParserConfiguration;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.reflections.Reflections;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Set;

public abstract class HttpParser<T extends ParsedItem> {

    private static final Log LOG = Log.forClass(HttpParser.class);

    public static <T extends HttpParser<? extends ParsedItem>> T getInstance(Class<T> base, String name, HttpClientFactory clientFactory, ParserConfiguration config) throws ClassNotFoundException {
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
                    LOG.info("Creating new parser for {}", parserName);

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
    private final URI url;
    private final Collection<Cookie> cookies;

    public HttpParser(HttpClientFactory clientFactory, String url) {
        this (clientFactory, URI.create(url));
    }

    public HttpParser(HttpClientFactory clientFactory, URI url) {
        this.clientFactory = clientFactory;
        this.url = url;

        cookies = Lists.newLinkedList();
    }

    protected void addCookie(String key, Object value) {
        final BasicClientCookie cookie = new BasicClientCookie(key, value.toString());

        cookie.setDomain(url.getHost());
        cookie.setPath("/");
        cookie.setSecure(true);

        cookies.add(cookie);
    }

    public URI getUrl() {
        return url;
    }

    protected HttpClient buildClient() {
        final HttpClient client = clientFactory.build();

        // Use the best guess cookie policy when deciding what cookie version to use
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);

        return client;
    }

    protected HttpContext buildContext() {
        final HttpContext context = new BasicHttpContext();

        final CookieStore cookieStore = new BasicCookieStore();

        for (Cookie cookie : cookies)
            cookieStore.addCookie(cookie);

        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        return context;
    }

    protected HttpUriRequest buildRequest() {
        return new HttpGet(url);
    }

    protected abstract Set<T> parse(Reader reader) throws Exception;

    protected Set<T> parse(HttpClient client, HttpContext context, HttpUriRequest request) throws Exception {
        final HttpResponse response = client.execute(request, context);
        final HttpEntity entity = response.getEntity();

        final InputStream in = response.getEntity().getContent();
        final String encoding = entity.getContentEncoding() == null ? Charset.defaultCharset().toString() : entity.getContentEncoding().getValue();

        try {
            if (LOG.isTraceEnabled())
                LOG.trace("Fetching result from {}", url);

            return this.parse(new InputStreamReader(in, encoding));
        }
        finally {
            in.close();
        }
    }
}
