package com.jamierf.mediamanager.parsing.ical;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.jamierf.mediamanager.parsing.FeedItem;

import java.util.Date;

public class CalendarItem implements FeedItem {

    @JsonProperty
    private final String uid;

    @JsonProperty
    private final String summary;

    @JsonProperty
    private final String description;

    @JsonProperty
    private final Date start;

    @JsonProperty
    private final Date end;

    @JsonCreator
    public CalendarItem(
            @JsonProperty("uid") String uid,
            @JsonProperty("summary") String summary,
            @JsonProperty("description") String description,
            @JsonProperty("start") Date start,
            @JsonProperty("end") Date end) {
        this.uid = uid;
        this.summary = summary;
        this.description = description;
        this.start = start;
        this.end = end;
    }

    public String getUid() {
        return uid;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CalendarItem that = (CalendarItem) o;

        if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("uid", uid)
                .add("summary", summary)
                .add("description", description)
                .add("start", start)
                .add("end", end)
                .toString();
    }
}
