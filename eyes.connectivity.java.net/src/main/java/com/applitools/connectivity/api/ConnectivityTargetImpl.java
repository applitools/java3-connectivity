package com.applitools.connectivity.api;

import com.applitools.eyes.AbstractProxySettings;
import com.applitools.eyes.EyesException;
import com.applitools.eyes.Logger;
import com.applitools.utils.ArgumentGuard;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.*;

public class ConnectivityTargetImpl extends ConnectivityTarget {
    static class ConnectionRetriever {
        private final URIBuilder builder;
        private final AbstractProxySettings abstractProxySettings;
        private final int timeout;
        private String[] acceptableResponseTypes = new String[]{};

        private ConnectionRetriever(URIBuilder builder, AbstractProxySettings abstractProxySettings, int timeout) {
            this.builder = builder;
            this.abstractProxySettings = abstractProxySettings;
            this.timeout = timeout;
        }

        public URLConnection createConnection() throws URISyntaxException, IOException {
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

            connection.setRequestProperty("Accept", builder.toString());
            return connection;
        }
    }

    private final ConnectionRetriever connectionRetriever;

    public ConnectivityTargetImpl(Logger logger, String baseUri, AbstractProxySettings proxySettings, int timeout) {
        super(logger);
        try {
            URIBuilder builder = new URIBuilder(baseUri);
            this.connectionRetriever = new ConnectionRetriever(builder, proxySettings, timeout);
        } catch (URISyntaxException e) {
            throw new EyesException("Invalid url", e);
        }

    }

    @Override
    public ConnectivityTarget path(String path) {
        ArgumentGuard.notNull(path, "path");
        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        connectionRetriever.builder.setPath(connectionRetriever.builder.getPath() + "/" + path);
        return this;
    }

    @Override
    public ConnectivityTarget queryParam(String name, String value) {
        ArgumentGuard.notNullOrEmpty(name, "name");
        ArgumentGuard.notNullOrEmpty(value, name);
        connectionRetriever.builder.setParameter(name, value);
        return this;
    }

    @Override
    public Request request(String... acceptableResponseTypes) {
        if (acceptableResponseTypes != null) {
            connectionRetriever.acceptableResponseTypes = acceptableResponseTypes;
        }
        return new RequestImpl(logger, connectionRetriever);
    }

    @Override
    public AsyncRequest asyncRequest(String... acceptableResponseTypes) {
        if (acceptableResponseTypes != null) {
            connectionRetriever.acceptableResponseTypes = acceptableResponseTypes;
        }
        return new AsyncRequestImpl(logger, connectionRetriever);
    }
}
