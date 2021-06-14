package com.applitools.connectivity.api;

import com.applitools.eyes.EyesException;
import com.applitools.eyes.Logger;
import com.applitools.utils.ArgumentGuard;

import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URISyntaxException;

public class RequestImpl extends Request {
    private final ConnectivityTargetImpl.ConnectionRetriever connectionRetriever;
    private HttpURLConnection connection;

    public RequestImpl(Logger logger, ConnectivityTargetImpl.ConnectionRetriever connectionRetriever) {
        super(logger);
        this.connectionRetriever = connectionRetriever;
        try {
            connection = (HttpURLConnection) connectionRetriever.createConnection();
        } catch (URISyntaxException | IOException e) {
            throw new EyesException("Failed creating connection", e);
        }
    }

    @Override
    public Request header(String name, String value) {
        ArgumentGuard.notNullOrEmpty(name, "name");
        connection.setRequestProperty(name, value);
        return this;
    }

    @Override
    protected Response methodInner(String method, Object data, String contentType) {
        ArgumentGuard.notNullOrEmpty(method, "method");
        try {
            sendRequest(connection, method, data, contentType);
            return new ResponseImpl(logger, connection);
        } catch (Throwable t) {
            connection.disconnect();
            try {
                connection = (HttpURLConnection) connectionRetriever.createConnection();
            } catch (URISyntaxException | IOException e) {
                throw new EyesException("Failed creating connection", e);
            }
            throw t;
        }

    }

    static void sendRequest(HttpURLConnection connection, String method, Object data, String contentType) {
        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException e) {
            throw new EyesException("Bad method", e);
        }

        if (contentType != null) {
            connection.setRequestProperty("Content-Type", contentType);
        }
        int contentLength = 0;
        byte[] dataBytes = null;
        if (data != null) {
            if (data instanceof String) {
                contentLength = ((String) data).length();
                dataBytes = ((String) data).getBytes();
            } else if (data instanceof byte[]) {
                contentLength = ((byte[]) data).length;
                dataBytes = (byte[]) data;
            } else {
                throw new EyesException("data can be string or byte array only");
            }
        }
        connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
        if (!method.equals(HttpMethod.GET)) {
            connection.setDoOutput(true);
        }
        try {
            connection.connect();
            if (dataBytes != null) {
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(dataBytes);
                outputStream.flush();
                outputStream.close();
            }

            // Wait for response
            connection.getResponseCode();
        } catch (Exception e) {
            connection.disconnect();
            throw new EyesException("Failed sending request", e);
        }
    }
}
