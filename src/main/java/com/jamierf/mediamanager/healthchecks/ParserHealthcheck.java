package com.jamierf.mediamanager.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Sets;
import com.jamierf.mediamanager.io.HttpParser;
import com.jamierf.mediamanager.managers.ParsingManager;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;

public class ParserHealthcheck extends HealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(ParserHealthcheck.class);

    private static boolean isReachable(String host, Duration timeout) {
        try {
            return InetAddress.getByName(host).isReachable((int) timeout.toMilliseconds());
        }
        catch (IOException e) {
            return false;
        }
    }

    private final Duration timeout;
    private final ParsingManager[] managers;

    public ParserHealthcheck(Duration timeout, ParsingManager ... managers) {
        this.timeout = timeout;

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

        for (String host : hosts) {
            try {
                final long startTime = System.currentTimeMillis();

                if (!ParserHealthcheck.isReachable(host, timeout)) {
                    final String errorMessage = String.format("Ping for %s timed out after %s", timeout, host);
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
