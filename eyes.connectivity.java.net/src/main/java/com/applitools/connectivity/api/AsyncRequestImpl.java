package com.applitools.connectivity.api;

import com.applitools.eyes.EyesException;
import com.applitools.eyes.Logger;
import com.applitools.utils.ArgumentGuard;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

public class AsyncRequestImpl extends AsyncRequest {
    private final ConnectivityTargetImpl.ConnectionRetriever connectionRetriever;
    private HttpURLConnection connection;

    public AsyncRequestImpl(Logger logger, ConnectivityTargetImpl.ConnectionRetriever connectionRetriever) {
        super(logger);
        this.connectionRetriever = connectionRetriever;
        try {
            connection = (HttpURLConnection) connectionRetriever.createConnection();
        } catch (URISyntaxException | IOException e) {
            throw new EyesException("Failed creating connection", e);
        }
    }

    @Override
    public AsyncRequest header(String name, String value) {
        ArgumentGuard.notNullOrEmpty(name, "name");
        if (connection.getRequestProperty(name) == null) {
            connection.setRequestProperty(name, value);
        }
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
            try {
                connection = (HttpURLConnection) connectionRetriever.createConnection();
            } catch (URISyntaxException | IOException e) {
                throw new EyesException("Failed creating connection", e);
            }
            callback.onFail(t);
        }
        return null;
    }
}
