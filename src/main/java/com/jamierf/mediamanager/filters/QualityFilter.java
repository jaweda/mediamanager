package com.jamierf.mediamanager.filters;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.jamierf.mediamanager.db.QualityDatabase;
import com.jamierf.mediamanager.models.NameAndQuality;
import io.dropwizard.util.Duration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Set;

public class QualityFilter implements Predicate<NameAndQuality> {

    private static final Logger LOG = LoggerFactory.getLogger(QualityFilter.class);

    private final QualityDatabase db;
    private final Set<String> primary;
    private final Set<String> secondary;
    private final Duration primaryTimeout;

    public QualityFilter(final QualityDatabase db, final Set<String> primary, final Set<String> secondary, final Duration primaryTimeout) {
        this.db = db;
        this.primary = primary;
        this.secondary = secondary;
        this.primaryTimeout = primaryTimeout;
    }

    @Override
    public boolean apply(@Nullable final NameAndQuality input) {
        if (input == null) {
            return false;
        }

        final DateTime now = DateTime.now();

        db.update(input.getName(), now);
        final Optional<DateTime> firstSeen = db.get(input.getName());
        final boolean trySecondary = firstSeen.isPresent() && firstSeen.get().isBefore(now.minus(primaryTimeout.toMilliseconds()));

        if (primary.contains(input.getQuality())) {
            LOG.trace("Accepting {}, {} is a primary quality", input.getName(), input.getQuality());
            return true;
        }

        if (trySecondary && secondary.contains(input.getQuality())) {
            LOG.trace("Accepting {}, {} is a secondary quality and first seen at {}", input.getName(), input.getQuality(), firstSeen);
            return true;
        }

        LOG.trace("Rejecting {}, {} not desired quality and first seen at {}", input.getName(), input.getQuality(), firstSeen);
        return false;
    }
}
