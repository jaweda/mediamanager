package com.jamierf.mediamanager.db;

import com.google.common.base.Optional;
import com.jamierf.mediamanager.models.Name;
import io.dropwizard.lifecycle.Managed;
import org.joda.time.DateTime;

public interface QualityDatabase extends Managed {
    void update(final Name name, final DateTime now);
    Optional<DateTime> get(final Name name);
}
