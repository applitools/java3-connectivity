package com.applitools.connectivity.api;

import com.applitools.eyes.Logger;
import com.applitools.utils.ArgumentGuard;

import java.net.HttpURLConnection;
import java.util.concurrent.Future;

public class AsyncRequestImpl extends AsyncRequest {
    private final HttpURLConnection connection;

    public AsyncRequestImpl(Logger logger, HttpURLConnection connection) {
        super(logger);
        this.connection = connection;
    }

    @Override
    public AsyncRequest header(String name, String value) {
        ArgumentGuard.notNullOrEmpty(name, "name");
        connection.setRequestProperty(name, value);
        return this;
    }

    @Override
    public Future<?> method(String method, AsyncRequestCallback callback, Object data, String contentType, boolean logIfError) {
        ArgumentGuard.notNullOrEmpty(method, "method");
        try {
            RequestImpl.sendRequest(connection, method, data, contentType);
            callback.onComplete(new ResponseImpl(logIfError ? logger : new Logger(), connection));
        } catch (Throwable t) {
            connection.disconnect();
            callback.onFail(t);
        }
        return null;
    }
}
