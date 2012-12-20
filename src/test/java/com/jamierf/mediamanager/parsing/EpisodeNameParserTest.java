package com.jamierf.mediamanager.parsing;

import com.google.common.collect.Maps;
import com.jamierf.mediamanager.models.Episode;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class EpisodeNameParserTest {

    private Map<String, Episode.Name> titles = Maps.newHashMap();

    @Before
    public void setUp() {
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
            final Episode.Name actual = EpisodeNameParser.parseFilename(title);

            assertEquals(expected, actual);
        }
    }
}
