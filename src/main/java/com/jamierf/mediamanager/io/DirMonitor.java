package com.jamierf.mediamanager.io;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yammer.dropwizard.logging.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DirMonitor {

	private static final Log LOG = Log.forClass(DirMonitor.class);

	private final Path root;
	private final FileFilter filter;
	private final WatchService watcher;

	private final Set<File> oldFiles;
	private final AtomicBoolean running;
	private final Collection<FileListener> listeners;

	private final ExecutorService bossPool;

	public DirMonitor(File file) throws IOException {
		this (file, null);
	}

	public DirMonitor(File file, FileFilter filter) throws IOException {
		if (!file.exists())
			throw new FileNotFoundException("Root directory not found: " + file);

		this.filter = filter;

		root = file.toPath();
		watcher = root.getFileSystem().newWatchService();

		oldFiles = Sets.newHashSet(root.toFile());
		running = new AtomicBoolean(false);
		listeners = Lists.newLinkedList();

		bossPool = Executors.newSingleThreadExecutor();

		root.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
	}

	public void addListener(FileListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeListener(FileListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void start() {
		if (!running.compareAndSet(false, true))
			throw new RuntimeException("DirMonitor already running");

		bossPool.execute(new Runnable() {
			@Override
			public void run() {
				listenForWatchEvents();
			}
		});

		this.processDirectory(root.toFile());
	}

	public void stop() {
		if (!running.compareAndSet(true, false))
			throw new RuntimeException("DirMonitor not running");

		try {
			watcher.close();
		}
		catch (IOException e) {
			// Ignore, this means we stopped
		}
	}

	private void processDirectory(File file) {
		if (file.isDirectory()) {
			// We are working with a directory, recursively process
			final File[] children = file.listFiles();
			for (File child : children)
				this.processDirectory(child);
		}

		this.processFile(file);
	}

	private void listenForWatchEvents() {
		while (running.get()) {
			try {
				final WatchKey key = watcher.take();

				for (final WatchEvent<?> event : key.pollEvents()) {
					if (event.kind() == StandardWatchEventKinds.OVERFLOW)
						continue;

					// Handle the new file
					final Path path = root.resolve((Path) event.context());
					this.processDirectory(path.toFile());
				}

				// Reset the key - if it fails then the directory is inaccessible, so break
				if (!key.reset()) {
                    LOG.error("Failed to reset watch key");

					break;
				}
			}
			catch (InterruptedException e) {
                LOG.error("Watch event thread interrupted", e);
			}
			catch (ClosedWatchServiceException e) {
				// Ignore, this means we stopped
			}
		}
	}

	private void processFile(final File file) {
		// Check it's a file we care about
		if (filter != null && !filter.accept(file))
			return;

		synchronized (oldFiles) {
			// Check if this is a file we have already seen
			if (oldFiles.contains(file))
				return;

			// Add this to our seen list
			oldFiles.add(file);
		}

		// We are working with a new file

		synchronized (listeners) {
			for (FileListener listener : listeners) {
				listener.onNewFile(file);
			}
		}
	}
}
