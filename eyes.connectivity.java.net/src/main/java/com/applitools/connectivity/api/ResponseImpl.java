package com.applitools.connectivity.api;

import com.applitools.eyes.EyesException;
import com.applitools.eyes.Logger;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseImpl extends Response {
    private final HttpURLConnection connection;

    public ResponseImpl(Logger logger, HttpURLConnection connection) {
        super(logger);
        this.connection = connection;
        readEntity();
        logIfError();
    }

    @Override
    public int getStatusCode() {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new EyesException("Failed reading response", e);
        }
    }

    @Override
    public String getStatusPhrase() {
        try {
            return connection.getResponseMessage();
        } catch (IOException e) {
            throw new EyesException("Failed reading response", e);
        }
    }

    @Override
    public String getHeader(String name, boolean ignoreCase) {
        ArgumentGuard.notNullOrEmpty(name, "name");
        Map<String, List<String>> headers = connection.getHeaderFields();
        if (!ignoreCase) {
            List<String> values = headers.get(name);
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            }

            return null;
        }

        for (String key : headers.keySet()) {
            if (name.equalsIgnoreCase(key)) {
                List<String> values = headers.get(name);
                if (values != null && !values.isEmpty()) {
                    return values.get(0);
                }

                return null;
            }
        }

        return null;
    }

    @Override
    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        for (String key : connection.getHeaderFields().keySet()) {
            if (key != null) {
                headers.put(key, getHeader(key, false));
            }
        }

        return headers;
    }

    @Override
    protected void readEntity() {
        try {
            InputStream inputStream;
            try {
                inputStream = connection.getInputStream();
            } catch (Exception e) {
                inputStream = connection.getErrorStream();
            }

            body = GeneralUtils.readInputStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new EyesException("Failed reading response", e);
        }
    }

    @Override
    public void close() {
        connection.disconnect();
    }
}
