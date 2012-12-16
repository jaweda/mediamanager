package com.jamierf.mediamanager.models;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Calendar;
import java.util.regex.Pattern;

public class Episode {

    public static class Name {

        private static final Pattern SHOW_TITLE_CLEAN_REGEX = Pattern.compile("[\\W]", Pattern.CASE_INSENSITIVE);

        private static String cleanShowTitle(String title) {
            return SHOW_TITLE_CLEAN_REGEX.matcher(title).replaceAll("").toLowerCase();
        }

        @JsonProperty
        private final String title;

        @JsonProperty
        private final int season;

        @JsonProperty
        private final int episode;

        @JsonProperty
        private final String quality;

        @JsonCreator
        public Name(
                @JsonProperty("title") String title,
                @JsonProperty("season") int season,
                @JsonProperty("episode") int episode,
                @JsonProperty("quality") String quality) {
            this.title = title;
            this.season = season;
            this.episode = episode;
            this.quality = quality;
        }

        public String getTitle() {
            return title;
        }

        @JsonIgnore
        public String getCleanedTitle() {
            return title == null ? null : Name.cleanShowTitle(title);
        }

        public int getSeason() {
            return season;
        }

        public int getEpisode() {
            return episode;
        }

        public String getQuality() {
            return quality;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Name name = (Name) o;

            if (episode != name.episode) return false;
            if (season != name.season) return false;
            if (title != null ? !title.equals(title) : name.title != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = title != null ? title.hashCode() : 0;
            result = 31 * result + season;
            result = 31 * result + episode;
            return result;
        }

        @Override
        public String toString() {
            return String.format("%s s%de%d", title, season, episode);
        }
    }

    @JsonProperty
    private final Name name;

    @JsonProperty
    private final Calendar notBefore;

    @JsonProperty
    private final Calendar notAfter;

    @JsonProperty
    private final State state;

    @JsonCreator
    public Episode(
            @JsonProperty("name") Name name,
            @JsonProperty("notBefore") Calendar notBefore,
            @JsonProperty("notAfter") Calendar notAfter,
            @JsonProperty("state") State state) {
        this.name = name;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.state = state;
    }

    public Episode copyWithState(State state) {
        return new Episode(name, notBefore, notAfter, state);
    }

    public Name getName() {
        return name;
    }

    @JsonIgnore
    public boolean isDesired() {
        if (state != State.DESIRED)
            return false;

        final Calendar now = Calendar.getInstance();
        if (notBefore != null && now.before(notBefore))
            return false;

        if (notAfter != null && now.after(notAfter))
            return false;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Episode episode = (Episode) o;

        if (name != null ? !name.equals(episode.name) : episode.name != null) return false;
        if (notAfter != null ? !notAfter.equals(episode.notAfter) : episode.notAfter != null) return false;
        if (notBefore != null ? !notBefore.equals(episode.notBefore) : episode.notBefore != null) return false;
        if (state != episode.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (notBefore != null ? notBefore.hashCode() : 0);
        result = 31 * result + (notAfter != null ? notAfter.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s (state: %s)", name, state);
    }
}
