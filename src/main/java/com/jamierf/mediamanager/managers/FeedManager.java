package com.jamierf.mediamanager.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jamierf.mediamanager.parsing.FeedItem;
import com.jamierf.mediamanager.parsing.FeedListener;
import com.jamierf.mediamanager.parsing.FeedParser;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.logging.Log;
import com.yammer.dropwizard.util.Duration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class FeedManager<T extends FeedItem> implements Managed, Runnable {

    private static final Log LOG = Log.forClass(FeedManager.class);

    private final Duration delay;
    private final ScheduledExecutorService bossPool;
    private final ExecutorService workerPool;
    private final Collection<FeedParser<T>> parsers;
    private final Collection<FeedListener<T>> listeners;
    private final Set<T> oldItems; // TODO: This should be a bounded queue, but needs efficient contains()
    private final AtomicReference<ScheduledFuture<?>> future;

    public FeedManager(Duration delay) {
        this.delay = delay;

        bossPool = Executors.newSingleThreadScheduledExecutor();
        workerPool = Executors.newCachedThreadPool();

        parsers = Lists.newLinkedList();
        listeners = Lists.newLinkedList();
        oldItems = Sets.newHashSet();

        future = new AtomicReference<ScheduledFuture<?>>();
    }

    @Override
    public void start() {
        // Only schedule if the future hasn't already been set
        if (!future.compareAndSet(null, bossPool.scheduleWithFixedDelay(this, 0, delay.toMilliseconds(), TimeUnit.MILLISECONDS)))
            throw new RuntimeException("Feeder is already running");

        if (LOG.isDebugEnabled())
            LOG.debug("Started with a delay of {}", delay);
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled())
            LOG.debug("Running feed parsers");

        // List to hold all parsed results, from all parsers
        final Set<T> items = Sets.newHashSet();
        final Collection<Exception> exceptions = new LinkedList<Exception>();

        // Run every parser and add the results
        synchronized (parsers) {
            for (final FeedParser<T> parser : parsers) {
                try {
                    // Fetch a list of items from this parser
                    final Set<T> parsedItems = parser.parse();
                    for (T item : parsedItems) {
                        // If we've already seen this item then skip it
                        if (oldItems.contains(item))
                            continue;

                        // Add the item to our set of new items, and add it to our seen items
                        items.add(item);

                        oldItems.add(item);
                    }
                }
                catch (Exception e) {
                    if (LOG.isDebugEnabled())
                        LOG.debug(e, "Caught exception while parsing feed");

                    exceptions.add(e);
                }
            }
        }

        // Alert every listener of each item and exception
        synchronized (listeners) {
            for (final FeedListener<T> listener : listeners) {
                workerPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (Throwable exception : exceptions)
                            listener.onException(exception);

                        for (T item : items)
                            listener.onNewItem(item);
                    }
                });
            }
        }
    }

    @Override
    public void stop() {
        if (future.get() == null)
            throw new RuntimeException("Feeder is not running");

        if (LOG.isDebugEnabled())
            LOG.debug("Shutting down");

        future.get().cancel(false);
        bossPool.shutdown();
        workerPool.shutdown();
    }

    public void addParser(FeedParser<T> parser) {
        synchronized (parsers) {
            parsers.add(parser);
        }
    }

    public void removeParser(FeedParser<T> parser) {
        synchronized (parsers) {
            parsers.remove(parser);
        }
    }

    public void addListener(FeedListener<T> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(FeedListener<T> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
}
