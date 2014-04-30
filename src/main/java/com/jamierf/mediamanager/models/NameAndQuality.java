package com.jamierf.mediamanager.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NameAndQuality implements Comparable<NameAndQuality> {

    @JsonProperty
    private final Name name;

    @JsonProperty
    private final String quality;

    @JsonCreator
    public NameAndQuality(
            @JsonProperty("name") Name name,
            @JsonProperty("quality") String quality) {
        this.name = name;
        this.quality = quality == null ? null : quality.toLowerCase();
    }

    public Name getName() {
        return name;
    }

    public String getQuality() {
        return quality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NameAndQuality that = (NameAndQuality) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (quality != null ? !quality.equals(that.quality) : that.quality != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (quality != null ? quality.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name.toString(), quality);
    }

    @Override
    public int compareTo(NameAndQuality o) {
        return name.compareTo(o.name);
    }
}
