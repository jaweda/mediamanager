package com.jamierf.mediamanager.io.retry;

import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.yammer.dropwizard.util.Duration;
import org.apache.http.conn.ConnectTimeoutException;

import java.net.SocketTimeoutException;
import java.util.Set;

import static org.apache.http.HttpStatus.*;

public class JerseyRetryManager extends InstrumentedRetryManager {
    public static final int SC_TOO_MANY_REQUESTS = 429;
    public final static ImmutableSet<Integer> DEFAULT_RETRIABLE_STATUS_CODES = ImmutableSet.of(
            SC_REQUEST_TIMEOUT, SC_METHOD_FAILURE, SC_TOO_MANY_REQUESTS, SC_INTERNAL_SERVER_ERROR, SC_BAD_GATEWAY,
            SC_SERVICE_UNAVAILABLE, SC_GATEWAY_TIMEOUT);
    public static final Duration DEFAULT_MAX_DURATION = Duration.hours(1L);

    protected final int maxRetries;
    protected final Set<Integer> retriableStatusCodes;
    protected final long expirationMS;

    public JerseyRetryManager(final Class<?> klass, final int maxRetries, final Duration maxDuration,
                              final Set<Integer> retriableStatusCodes) {
        super(klass);
        this.maxRetries = maxRetries;
        this.expirationMS = ((maxDuration == null)
                ? DEFAULT_MAX_DURATION
                : maxDuration).toMilliseconds();
        this.retriableStatusCodes = (retriableStatusCodes == null)
                ? DEFAULT_RETRIABLE_STATUS_CODES
                : retriableStatusCodes;
    }

    @Override
    protected boolean retryFailure(final Exception exception, final int attempts, final long startTimeMS) {
        if (attempts > maxRetries) return false;
        if (System.currentTimeMillis() - startTimeMS > expirationMS) return false;

        if (exception instanceof UniformInterfaceException) {
            final UniformInterfaceException uie = (UniformInterfaceException) exception;

            uie.getResponse().bufferEntity();
            // ensure [re]tried response is closed

            if (retryFailure(uie.getResponse(), attempts, startTimeMS)) {
                return true;
            }
        }

        if (exception instanceof ClientHandlerException) {
            final ClientHandlerException che = (ClientHandlerException) exception;

            // Retry on timeouts
            if (che.getCause() instanceof ConnectTimeoutException || che.getCause() instanceof SocketTimeoutException) {
                return true;
            }
        }

        return false;
    }

    protected boolean retryFailure(final ClientResponse response, final int attempts, final long startTimeMS) {
        return retriableStatusCodes.contains(response.getStatus());
    }
}
