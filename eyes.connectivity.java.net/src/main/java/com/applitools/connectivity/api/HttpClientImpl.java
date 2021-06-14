package com.applitools.connectivity.api;

import com.applitools.eyes.AbstractProxySettings;
import com.applitools.eyes.Logger;
import com.applitools.eyes.logging.Stage;
import com.applitools.utils.GeneralUtils;
import com.applitools.utils.NetworkUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HttpClientImpl extends HttpClient {
    public HttpClientImpl(Logger logger, int timeout, AbstractProxySettings abstractProxySettings) {
        super(logger, timeout, abstractProxySettings);
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(NetworkUtils.getDisabledSSLContext().getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            GeneralUtils.logExceptionStackTrace(logger, Stage.GENERAL, e);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
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
