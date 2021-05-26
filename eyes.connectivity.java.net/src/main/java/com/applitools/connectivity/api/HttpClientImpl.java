package com.applitools.connectivity.api;

import com.applitools.eyes.AbstractProxySettings;
import com.applitools.eyes.Logger;

import java.net.URI;

public class HttpClientImpl extends HttpClient {
    public HttpClientImpl(Logger logger, int timeout, AbstractProxySettings abstractProxySettings) {
        super(logger, timeout, abstractProxySettings);
    }

    @Override
    public ConnectivityTarget target(URI baseUrl) {
        return new ConnectivityTargetImpl(logger, baseUrl.toString(), abstractProxySettings, timeout);
    }

    @Override
    public ConnectivityTarget target(String path) {
        return new ConnectivityTargetImpl(logger, path, abstractProxySettings, timeout);
    }

    @Override
    public void close() {
        isClosed = true;
    }
}
