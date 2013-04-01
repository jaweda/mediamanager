package com.jamierf.mediamanager.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

public class Episode {

    public static class Name {

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
            this.quality = quality == null ? null : quality.toLowerCase();
        }

        public String getTitle() {
            return title;
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
            final StringBuilder builder = new StringBuilder();

            builder.append(title);
            builder.append(" ");

            // Only append season if we have one
            if (season > 0)
                builder.append("s").append(Strings.padStart(String.valueOf(season), 2, '0'));

            builder.append("e").append(Strings.padStart(String.valueOf(episode), 2, '0'));

            // If it has a quality rating then include that
            if (!Strings.isNullOrEmpty(quality)) {
                builder.append(" ");
                builder.append(quality);
            }

            return builder.toString();
        }
    }

    @JsonProperty
    private final Name name;

    @JsonProperty
    private final State state;

    @JsonCreator
    public Episode(
            @JsonProperty("name") Name name,
            @JsonProperty("state") State state) {
        this.name = name;
        this.state = state;
    }

    public Episode copyWithState(State state) {
        return new Episode(name, state);
    }

    public Name getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    @JsonIgnore
    public boolean isDesired() {
        return state == State.DESIRED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Episode episode = (Episode) o;

        if (name != null ? !name.equals(episode.name) : episode.name != null) return false;
        if (state != episode.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, state);
    }
}
