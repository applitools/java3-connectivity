package com.applitools.eyes;

import java.net.URI;
import java.net.URISyntaxException;

public class ProxySettings extends AbstractProxySettings {
    public ProxySettings(String uri, int port, String username, String password) {
        super(uri, port, username, password);
        try {
            int portFromUrl = new URI(uri).getPort();
            this.uri = new URI(uri).getHost();
            this.port = portFromUrl == -1 ? port : portFromUrl;
        } catch (URISyntaxException e) {
            throw new EyesException("Failed setting proxy", e);
        }
    }

    public ProxySettings(String uri, int port) {
        this(uri, port, null, null);
    }

    public ProxySettings(String uri, String username, String password) {
        this(uri, 8888, username, password);
    }

    public ProxySettings(String uri) {
        this(uri, 8888);
    }

    public ProxySettings() {
        super();
    }
}
