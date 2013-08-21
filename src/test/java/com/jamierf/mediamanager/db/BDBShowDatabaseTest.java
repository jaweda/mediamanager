package com.jamierf.mediamanager.db;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.jamierf.mediamanager.models.Episode;
import com.jamierf.mediamanager.models.State;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class BDBShowDatabaseTest {

    private static final Episode TEST_EPISODE = new Episode(new Episode.Name("test", 1, 1, "720p"), State.DESIRED);

    private File file;
    private BDBShowDatabase db;

    @Before
    public void setUp() throws Exception {
        file = Files.createTempDir();
        db = new BDBShowDatabase(file);
        db.start();
    }

    @After
    public void tearDown() throws Exception {
        db.stop();
        FileUtils.deleteDirectory(file);
    }

    @Test
    public void testAddEpisode() throws IOException {
        assertTrue(db.getAllEpisodes().isEmpty());
        db.addIfNotExists(TEST_EPISODE);
        assertFalse(db.getAllEpisodes().isEmpty());
    }

    @Test
    public void testGetEpisode() throws IOException {
        db.addIfNotExists(TEST_EPISODE);

        final Optional<Episode> result = db.get(TEST_EPISODE.getName());
        assertTrue(result.isPresent());
        assertEquals(TEST_EPISODE, result.get());
    }

    @Test
    public void testAllEpisodes() throws IOException {
        db.addIfNotExists(new Episode(new Episode.Name("test", 1, 1, "720p"), State.DESIRED));
        db.addIfNotExists(new Episode(new Episode.Name("test", 1, 2, "720p"), State.DESIRED));
        db.addIfNotExists(new Episode(new Episode.Name("test", 1, 3, "720p"), State.EXISTS));

        assertEquals(3, db.getAllEpisodes().size());
    }

    @Test
    public void testDesiredEpisodes() throws IOException {
        db.addIfNotExists(new Episode(new Episode.Name("test", 1, 1, "720p"), State.DESIRED));
        db.addIfNotExists(new Episode(new Episode.Name("test", 1, 2, "720p"), State.DESIRED));
        db.addIfNotExists(new Episode(new Episode.Name("test", 1, 3, "720p"), State.EXISTS));

        assertEquals(2, db.getDesiredEpisodes().size());
    }

    @Test
    public void testEpisodeQualityIgnored() throws IOException {
        db.addIfNotExists(new Episode(new Episode.Name("test", 1, 1, "720p"), State.DESIRED));

        final Optional<Episode> result = db.get(new Episode.Name("test", 1, 1, "1080p"));
        assertTrue(result.isPresent());
    }
}
