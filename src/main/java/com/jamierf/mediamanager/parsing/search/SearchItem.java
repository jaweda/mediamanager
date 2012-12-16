package com.jamierf.mediamanager.parsing.search;

import com.google.common.base.Objects;
import com.jamierf.mediamanager.io.ParsedItem;
import com.jamierf.mediamanager.parsing.DownloadableItem;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URL;

public class SearchItem implements ParsedItem, DownloadableItem {

    @JsonProperty
    private final int id;

    @JsonProperty
    private final String title;

    @JsonProperty
    private final URL link;

    public SearchItem(
            @JsonProperty("id") int id,
            @JsonProperty("title") String title,
            @JsonProperty("url") URL link) {
        this.id = id;
        this.title = title;
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public URL getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchItem that = (SearchItem) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("title", title)
                .add("link", link)
                .toString();
    }
}
