package com.jamierf.mediamanager.healthchecks;

import com.google.common.collect.Sets;
import com.jamierf.mediamanager.io.HttpParser;
import com.jamierf.mediamanager.managers.ParsingManager;
import com.yammer.dropwizard.logging.Log;
import com.yammer.dropwizard.util.Duration;
import com.yammer.metrics.core.HealthCheck;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

public class ParserHealthcheck extends HealthCheck {

    private static final Duration PING_TIMEOUT = Duration.seconds(5);

    private static final Log LOG = Log.forClass(ParserHealthcheck.class);

    private static boolean isReachable(String host, long timeout) {
        try {
            return InetAddress.getByName(host).isReachable((int) timeout);
        }
        catch (IOException e) {
            return false;
        }
    }

    private final ParsingManager[] managers;

    public ParserHealthcheck(ParsingManager ... managers) {
        super("parsers");

        this.managers = managers;
    }

    private Collection<String> collectHosts() {
        final Collection<String> hosts = Sets.newHashSet();

        // Collect all hosts from all parsers in to a single set
        for (ParsingManager manager : managers)
            for (HttpParser<?> parser : manager.getParsers())
                hosts.add(parser.getUrl().getHost());

        return hosts;
    }

    @Override
    protected Result check() throws Exception {
        final Collection<String> hosts = this.collectHosts();
        final long timeout = PING_TIMEOUT.toMilliseconds();

        for (String host : hosts) {
            try {
                final long startTime = System.currentTimeMillis();

                if (!ParserHealthcheck.isReachable(host, timeout)) {
                    final String errorMessage = String.format("Ping for %s timed out after %s", PING_TIMEOUT, host);
                    LOG.warn(errorMessage);

                    return Result.unhealthy(errorMessage);
                }

                if (LOG.isTraceEnabled()) {
                    final long duration = System.currentTimeMillis() - startTime;
                    LOG.trace("Ping for {} took {}ms", host, duration);
                }
            }
            catch (Exception e) {
                return Result.unhealthy(String.format("Error contacting host: %s", host));
            }
        }

        return Result.healthy();
    }
}
