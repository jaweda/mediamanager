package com.jamierf.mediamanager.parsing.rss;

import com.google.common.base.Objects;
import com.jamierf.mediamanager.parsing.DownloadableItem;
import com.jamierf.mediamanager.parsing.FeedItem;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URL;
import java.util.Date;

public class RSSItem implements FeedItem, DownloadableItem {

    @JsonProperty
	private final String guid;

    @JsonProperty
	private final String title;

    @JsonProperty
	private final Date date;

    @JsonProperty
	private final URL link;

    @JsonProperty
	private final String description;

    @JsonCreator
	public RSSItem(
            @JsonProperty("guid") String guid,
            @JsonProperty("title") String title,
            @JsonProperty("date") Date date,
            @JsonProperty("link") URL link,
            @JsonProperty("description") String description) {
		this.guid = guid;
		this.title = title;
		this.date = date;
		this.link = link;
		this.description = description;
	}

	public String getGuid() {
		return guid;
	}

	public String getTitle() {
		return title;
	}

	public Date getDate() {
		return date;
	}

	public URL getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RSSItem rssItem = (RSSItem) o;

        if (guid != null ? !guid.equals(rssItem.guid) : rssItem.guid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return guid != null ? guid.hashCode() : 0;
    }

    @Override
	public String toString() {
		return Objects.toStringHelper(this)
                .add("guid", guid)
                .add("title", title)
                .add("date", date)
                .add("link", link)
                .add("description", description)
                .toString();
	}
}
