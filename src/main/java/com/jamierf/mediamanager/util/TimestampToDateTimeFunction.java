package com.jamierf.mediamanager.util;

import com.google.common.base.Function;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

public class TimestampToDateTimeFunction implements Function<Long, DateTime> {

    public static final Function<Long, DateTime> INSTANCE = new TimestampToDateTimeFunction();

    @Nullable
    @Override
    public DateTime apply(@Nullable final Long input) {
        return new DateTime(input);
    }
}
