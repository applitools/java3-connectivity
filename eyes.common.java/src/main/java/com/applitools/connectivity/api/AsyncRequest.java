package com.applitools.connectivity.api;

import com.applitools.eyes.Logger;
import com.applitools.utils.GeneralUtils;

import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Wrapper for the asynchronous request api of the connectivity packages
 */
public abstract class AsyncRequest {

    private static final int REQUEST_TIMEOUT = 60 * 1000;
    private static final int SLEEP_DURATION = 5000;
    private int timePassed = 0;

    protected Logger logger;
    protected final String requestId;

    public AsyncRequest(Logger logger) {
        this.logger = logger;
        this.requestId = UUID.randomUUID().toString();
    }

    /**
     * Add a new http header to the request
     *
     * @param name  The header name
     * @param value The header value
     * @return An {@link AsyncRequest} object updated with the given header
     */
    public abstract AsyncRequest header(String name, String value);

    /**
     * @param method      The http method for the request
     * @param callback    To be called when the response is received
     * @param data        The data to send with the request. If null, no data will be sent.
     * @param contentType The data content type.  If null, no data will be sent.
     * @param logIfError  If true, a detailed log will be written in case of an error.
     * @return Response from the server
     */
    public abstract Future<?> method(String method, AsyncRequestCallback callback, Object data, String contentType, boolean logIfError);

    public Future<?> method(final String method, final AsyncRequestCallback callback, final Object data, final String contentType) {
        header("x-applitools-request-id", requestId);
        logger.verbose(String.format("Sending async request to the server. ID: %s, Type: %s", requestId, method));
        return method(method, new AsyncRequestCallback() {
            @Override
            public void onComplete(Response response) {
                logger.verbose(String.format("Async request onComplete. ID: %s, Type: %s", requestId, method));
                callback.onComplete(response);
            }

            @Override
            public void onFail(Throwable throwable) {
                if (timePassed >= REQUEST_TIMEOUT) {
                    logger.verbose(String.format("Async request onFail. ID: %s, Type: %s", requestId, method));
                    callback.onFail(throwable);
                    return;
                }

                try {
                    Thread.sleep(SLEEP_DURATION);
                } catch (InterruptedException ignored) {}

                timePassed += SLEEP_DURATION;
                GeneralUtils.logExceptionStackTrace(logger, throwable);
                logger.verbose(String.format("Failed sending request. Trying again. ID: %s, Type: %s", requestId, method));
                method(method, callback, data, contentType);
            }
        }, data, contentType, true);
    }
}
