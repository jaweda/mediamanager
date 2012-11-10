package com.jamierf.mediamanager.models;

import com.google.common.collect.Maps;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.util.Duration;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

public class ShowDatabase implements Managed {

    private static final Pattern SHOW_TITLE_CLEAN_REGEX = Pattern.compile("[\\W]", Pattern.CASE_INSENSITIVE);

    private static String showTitleToKey(String title) {
        return SHOW_TITLE_CLEAN_REGEX.matcher(title).replaceAll("").toLowerCase();
    }

    @JsonIgnore
    private final Duration beforeDuration;

    @JsonIgnore
    private final Duration afterDuration;

    @JsonProperty
    private final Map<String, Show> shows;

    public ShowDatabase(Duration beforeDuration, Duration afterDuration) {
        this.beforeDuration = beforeDuration;
        this.afterDuration = afterDuration;

        shows = Maps.newHashMap();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    public boolean addDesiredEpisode(String title, int s, int e, Date endTime) {
        final Calendar notBefore = Calendar.getInstance();
        notBefore.setTime(endTime);
        notBefore.add(Calendar.MINUTE, -(int) beforeDuration.toMinutes());

        final Calendar notAfter = Calendar.getInstance();
        notAfter.setTime(endTime);
        notAfter.add(Calendar.MINUTE, +(int) afterDuration.toMinutes());

        // Check we aren't already past the not after time
        final Calendar now = Calendar.getInstance();
        if (now.after(notAfter))
            return false;

        final String key = ShowDatabase.showTitleToKey(title);

        if (!shows.containsKey(key))
            shows.put(key, new Show());

        final Show show = shows.get(key);
        final Episode episode = show.getOrCreateEpisode(s, e);

        episode.setNotBefore(notBefore);
        episode.setNotAfter(notAfter);
        episode.setState(State.DESIRED);

        return true;
    }

    public Episode getDesiredEpisode(String title, int s, int e) {
        final String key = ShowDatabase.showTitleToKey(title);

        final Show show = shows.get(key);
        if (show == null)
            return null;

        final Episode episode = show.getEpisode(s, e);
        if (episode == null || !episode.isDesired())
            return null;

        return episode;
    }
}
