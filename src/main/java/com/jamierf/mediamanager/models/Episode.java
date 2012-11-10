package com.jamierf.mediamanager.models;

import com.google.common.base.Objects;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Calendar;

public class Episode {

    public static class Name {
        private final String title;
        private final int season;
        private final int episode;
        private final String quality;

        public Name(String title, int season, int episode, String quality) {
            this.title = title;
            this.season = season;
            this.episode = episode;
            this.quality = quality;
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + episode;
            result = prime * result
                    + ((quality == null) ? 0 : quality.hashCode());
            result = prime * result + season;
            result = prime * result + ((title == null) ? 0 : title.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Name other = (Name) obj;
            if (episode != other.episode)
                return false;
            if (quality == null) {
                if (other.quality != null)
                    return false;
            } else if (!quality.equals(other.quality))
                return false;
            if (season != other.season)
                return false;
            if (title == null) {
                if (other.title != null)
                    return false;
            } else if (!title.equals(other.title))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return String.format("%s s%de%d (%s)", title, season, episode, quality);
        }
    }

    @JsonProperty
    private final int season;

    @JsonProperty
    private final int episode;

    @JsonProperty
    private Calendar notBefore;

    @JsonProperty
    private Calendar notAfter;

    @JsonProperty
    private State state;

    public Episode(int season, int episode) {
        this.season = season;
        this.episode = episode;

        notBefore = null;
        notAfter = null;
        state = null;
    }

    public void setNotBefore(Calendar notBefore) {
        this.notBefore = notBefore;
    }

    public void setNotAfter(Calendar notAfter) {
        this.notAfter = notAfter;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getSeason() {
        return season;
    }

    public int getEpisode() {
        return episode;
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

        Episode episode1 = (Episode) o;

        if (episode != episode1.episode) return false;
        if (season != episode1.season) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = season;
        result = 31 * result + episode;
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("season", season)
                .add("episode", episode)
                .add("state", state)
                .add("notBefore", notBefore)
                .add("notAfter", notAfter)
                .toString();
    }
}
