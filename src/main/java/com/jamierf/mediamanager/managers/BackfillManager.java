package com.jamierf.mediamanager.managers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jamierf.mediamanager.db.ShowDatabase;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.Name;
import com.jamierf.mediamanager.parsing.ItemListener;
import com.jamierf.mediamanager.parsing.search.SearchItem;
import com.jamierf.mediamanager.parsing.search.SearchParser;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class BackfillManager implements Managed, Runnable, ParsingManager {

    private static final Logger LOG = LoggerFactory.getLogger(BackfillManager.class);

    private static final Duration THROTTLE_DELAY = Duration.seconds(5);
    private static final Duration START_DELAY = Duration.minutes(1);

    private static final Random random;

    static {
        random = new Random();
    }

    private final ShowDatabase shows;
    private final Duration delay;
    private final ScheduledExecutorService bossPool;
    private final ExecutorService workerPool;
    private final Collection<SearchParser> parsers;
    private final Collection<ItemListener<SearchItem>> listeners;
    private final AtomicReference<ScheduledFuture<?>> future;

    public BackfillManager(ShowDatabase shows, Duration delay) {
        this.shows = shows;
        this.delay = delay;

        bossPool = Executors.newSingleThreadScheduledExecutor();
        workerPool = Executors.newCachedThreadPool();

        parsers = Lists.newLinkedList();
        listeners = Lists.newLinkedList();

        future = new AtomicReference();
    }

    @Override
    public synchronized void start() throws Exception {
        if (future.get() != null)
            throw new RuntimeException("Backfiller is already running");

        this.schedule();

        if (LOG.isDebugEnabled())
            LOG.debug("Started with a delay of {}", delay);
    }

    public synchronized void schedule() {
        final ScheduledFuture<?> future = this.future.getAndSet(bossPool.scheduleWithFixedDelay(this, START_DELAY.toMilliseconds(), delay.toMilliseconds(), TimeUnit.MILLISECONDS));
        // If there was an existing future, cancel it
        if (future != null)
            future.cancel(false);

        if (LOG.isTraceEnabled())
            LOG.trace("Scheduled a new backfill in {}", START_DELAY);
    }

    @Override
    public void run() {
        if (parsers.isEmpty()) {
            if (LOG.isDebugEnabled())
                LOG.debug("Skipping backfill, we have no search parsers");

            return;
        }

        try {
            final Collection<Episode> episodes = shows.getDesiredEpisodes();
            if (episodes.isEmpty()) {
                if (LOG.isDebugEnabled())
                    LOG.debug("Skipping backfill, we have all desired episodes");

                return;
            }

            if (LOG.isInfoEnabled())
                LOG.info("Starting a backfill for {} episodes", episodes.size());

            final int throttleDelayMS = (int) THROTTLE_DELAY.toMilliseconds();
            for (Episode episode : episodes) {
                try {
                    this.search(episode.getName());

                    // sleep for a delay around THROTTLE_DELAY, +/- 50%
                    final long delay = throttleDelayMS - (throttleDelayMS / 2) + random.nextInt(throttleDelayMS);
                    Thread.sleep(delay);
                }
                catch (Exception e) {
                    LOG.error("Failed searching for episode: " + episode.getName(), e);
                }
            }
        }
        catch (IOException e) {
            LOG.error("Failed to fetch list of desired episodes", e);
        }
    }

    public void search(Name name) {
        final String query = name.toString();

        if (LOG.isDebugEnabled())
            LOG.debug("Running search parsers for: {}", query);

        // List to hold all parsed results, from all parsers
        final Set<SearchItem> items = Sets.newHashSet();
        final Collection<Exception> exceptions = new LinkedList<Exception>();

        // Run every parser and add the results
        synchronized (parsers) {
            for (final SearchParser parser : parsers) {
                try {
                    // Fetch a list of items from this parser
                    final Set<SearchItem> parsedItems = parser.search(query);
                    items.addAll(parsedItems);
                }
                catch (ClientHandlerException e) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("Timeout connecting to {}", parser.getUrl());

                    exceptions.add(e);
                }
                catch (UniformInterfaceException e) {
                    final int status = e.getResponse().getStatus();
                    if (LOG.isDebugEnabled())
                        LOG.debug("HTTP error {} from {}", status, parser.getUrl());

                    exceptions.add(e);
                }
                catch (Exception e) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("Caught exception while parsing search", e);

                    exceptions.add(e);
                }
            }
        }

        // Alert every listener of each item and exception
        synchronized (listeners) {
            for (final ItemListener<SearchItem> listener : listeners) {
                workerPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (Throwable exception : exceptions)
                            listener.onException(exception);

                        for (SearchItem item : items)
                            listener.onNewItem(item);
                    }
                });
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (future.get() == null)
            throw new RuntimeException("Backfiller is not running");

        if (LOG.isDebugEnabled())
            LOG.debug("Shutting down");

        future.get().cancel(false);
        bossPool.shutdown();
        workerPool.shutdown();
    }

    public void addParser(SearchParser parser) {
        synchronized (parsers) {
            parsers.add(parser);
        }
    }

    public Collection<SearchParser> getParsers() {
        synchronized (parsers) {
            return ImmutableSet.copyOf(parsers);
        }
    }

    public void removeParser(SearchParser parser) {
        synchronized (parsers) {
            parsers.remove(parser);
        }
    }

    public void addListener(ItemListener<SearchItem> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(ItemListener<SearchItem> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
}
