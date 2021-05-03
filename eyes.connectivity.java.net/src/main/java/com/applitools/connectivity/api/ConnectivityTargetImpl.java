package com.applitools.connectivity.api;

import com.applitools.eyes.AbstractProxySettings;
import com.applitools.eyes.EyesException;
import com.applitools.eyes.Logger;
import com.applitools.utils.ArgumentGuard;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.*;

public class ConnectivityTargetImpl extends ConnectivityTarget {
    private final URIBuilder builder;
    private final AbstractProxySettings abstractProxySettings;
    private final int timeout;

    public ConnectivityTargetImpl(Logger logger, String baseUri, AbstractProxySettings proxySettings, int timeout) {
        super(logger);
        try {
            this.builder = new URIBuilder(baseUri);
        } catch (URISyntaxException e) {
            throw new EyesException("Invalid url", e);
        }

        this.abstractProxySettings = proxySettings;
        this.timeout = timeout;
    }

    @Override
    public ConnectivityTarget path(String path) {
        ArgumentGuard.notNull(path, "path");
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        builder.setPath(builder.getPath() + "/" + path);
        return this;
    }

    @Override
    public ConnectivityTarget queryParam(String name, String value) {
        ArgumentGuard.notNullOrEmpty(name, "name");
        ArgumentGuard.notNullOrEmpty(value, name);
        builder.setParameter(name, value);
        return this;
    }

    @Override
    public Request request(String... acceptableResponseTypes) {
        try {
            return new RequestImpl(logger, (HttpURLConnection) addAcceptHeader(createConnection(), acceptableResponseTypes));
        } catch (IOException | URISyntaxException e) {
            throw new EyesException("Failed creating request", e);
        }
    }

    @Override
    public AsyncRequest asyncRequest(String... acceptableResponseTypes) {
        try {
            return new AsyncRequestImpl(logger, (HttpURLConnection) addAcceptHeader(createConnection(), acceptableResponseTypes));
        } catch (IOException | URISyntaxException e) {
            throw new EyesException("Failed creating request", e);
        }
    }

    private URLConnection createConnection() throws IOException, URISyntaxException {
        URL url = builder.build().toURL();
        URLConnection connection;
        if (abstractProxySettings == null) {
            connection = url.openConnection();
        } else {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(abstractProxySettings.getUri(), abstractProxySettings.getPort()));
            if (abstractProxySettings.getUsername() != null && abstractProxySettings.getPassword() != null) {
                Authenticator authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(abstractProxySettings.getUsername(), abstractProxySettings.getPassword().toCharArray());
                    }
                };

                Authenticator.setDefault(authenticator);
            }

            connection = url.openConnection(proxy);
        }

        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setDoInput(true);
        return connection;
    }

    private URLConnection addAcceptHeader(URLConnection urlConnection, String[] acceptableResponseTypes) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (String item : acceptableResponseTypes) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            builder.append(item);
        }

        urlConnection.setRequestProperty("Accept", builder.toString());
        return urlConnection;
    }
}
