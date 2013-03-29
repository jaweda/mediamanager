package com.jamierf.mediamanager.parsing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jamierf.mediamanager.models.Episode;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class EpisodeNameParserTest {

    private EpisodeNameParser episodeNameParser;
    private Map<String, Episode.Name> titles;

    @Before
    public void setUp() {
        episodeNameParser = new EpisodeNameParser(ImmutableMap.of(
                "bb theory", "the big bang theory"
        ));

        titles = Maps.newHashMap();

        titles.put("Tron.Uprising.S01E13.720p.HDTV.x264-2HD", new Episode.Name("Tron Uprising", 1, 13, "720p"));
        titles.put("Tron.Uprising.S01E13.HDTV.x264-2HD", new Episode.Name("Tron Uprising", 1, 13, "HDTV"));
        titles.put("T.I.and.Tiny.The.Family.Hustle.S02E16.HDTV.x264-CRiMSON", new Episode.Name("T I and Tiny The Family Hustle", 2, 16, "HDTV"));
        titles.put("Survivor.S25E15.Reunion.720p.HDTV.x264-2HD", new Episode.Name("Survivor", 25, 15, "720p"));
        titles.put("Survivor.S25E15.Reunion.HDTV.x264-2HD", new Episode.Name("Survivor", 25, 15, "HDTV"));
        titles.put("The.Real.Housewives.of.Atlanta.S05E06.HDTV.x264-CRiMSON", new Episode.Name("The Real Housewives of Atlanta", 5, 6, "HDTV"));
        titles.put("Royal.Pains.S04E15.PROPER.720p.HDTV.X264-DIMENSION", new Episode.Name("Royal Pains", 4, 15, "HDTV"));
    }

    @Test
    public void testCorrectParsing() throws IOException {
        for (String title : titles.keySet()) {
            final Episode.Name expected = titles.get(title);
            final Episode.Name actual = episodeNameParser.parseFilename(title);

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testCleanEpisodeTitles() {
        assertEquals("Parenthood", EpisodeNameParser.cleanTitle("parenthood"));
        assertEquals("Tron Uprising", EpisodeNameParser.cleanTitle("Tron.Uprising"));
        assertEquals("Greys Anatomy", EpisodeNameParser.cleanTitle("greys.anatomy"));
    }

    @Test
    public void testEpisodeWithoutSeason() {
        assertEquals(new Episode.Name("Planet Earth", 0, 3, "720p"), episodeNameParser.parseFilename("planet.earth.part03.720p.hddvd.x264-medieval.mkv"));
    }

    @Test
    public void testAliases() {
        assertEquals(new Episode.Name("The Big Bang Theory", 3, 8, "720p"), episodeNameParser.parseFilename("bb.theory.s03e08.720p.hdtv.264"));
    }
}
