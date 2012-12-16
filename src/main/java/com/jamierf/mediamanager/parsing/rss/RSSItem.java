package com.jamierf.mediamanager.parsing.rss;

import com.google.common.base.Objects;
import com.jamierf.mediamanager.parsing.DownloadableItem;
import com.jamierf.mediamanager.parsing.FeedItem;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;
import java.util.Date;

public class RSSItem implements FeedItem, DownloadableItem {

    @JsonProperty
	private final String uid;

    @JsonProperty
	private final String title;

    @JsonProperty
	private final Date date;

    @JsonProperty
	private final URI link;

    @JsonProperty
	private final String description;

    @JsonCreator
	public RSSItem(
            @JsonProperty("uid") String uid,
            @JsonProperty("title") String title,
            @JsonProperty("date") Date date,
            @JsonProperty("link") URI link,
            @JsonProperty("description") String description) {
		this.uid = uid;
		this.title = title;
		this.date = date;
		this.link = link;
		this.description = description;
	}

	public String getUid() {
		return uid;
	}

	public String getTitle() {
		return title;
	}

	public Date getDate() {
		return date;
	}

	public URI getLink() {
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

        if (uid != null ? !uid.equals(rssItem.uid) : rssItem.uid != null) return false;

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
                .add("title", title)
                .add("date", date)
                .add("link", link)
                .add("description", description)
                .toString();
	}
}
