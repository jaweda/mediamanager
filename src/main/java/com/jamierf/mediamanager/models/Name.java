package com.jamierf.mediamanager.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Name implements Comparable<Name> {

    @JsonProperty
    private final String title;

    @JsonProperty
    private final int season;

    @JsonProperty
    private final int episode;

    @JsonCreator
    public Name(
            @JsonProperty("title") String title,
            @JsonProperty("season") int season,
            @JsonProperty("episode") int episode) {
        this.title = title;
        this.season = season;
        this.episode = episode;
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

        return builder.toString();
    }

    @Override
    public int compareTo(Name o) {
        final String thisName = this.toString();
        final String otherName = o.toString();

        return thisName.compareTo(otherName);
    }
}
