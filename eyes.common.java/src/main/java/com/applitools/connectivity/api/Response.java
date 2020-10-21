package com.applitools.connectivity.api;

import com.applitools.eyes.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class Response {

    protected Logger logger;

    protected byte[] body;

    public Response(Logger logger) {
        this.logger = logger;
    }

    public abstract int getStatusCode();

    public abstract String getStatusPhrase();

    /**
     * Get a response header
     *
     * @param name       The name of the header
     * @param ignoreCase If true, ignores case
     * @return The value of the header
     */
    public abstract String getHeader(String name, boolean ignoreCase);

    protected abstract Map<String,String> getHeaders();

    protected abstract void readEntity();

    public byte[] getBody() {
        return body;
    }

    public String getBodyString() {
        return new String(body);
    }

    public abstract void close();

    public void logIfError() {
        try {
            if (getStatusCode() >= 300) {
                logger.log(String.format("Got invalid response from the server. Status code: %s. Status Phrase: %s. Headers: %s. Response body: %s",
                        getStatusCode(), getStatusPhrase(), getHeaders(), getBodyString()));
            }
        } catch (Exception e) {
            logger.log(String.format("Failed logging the response body. Status code: %s", getStatusCode()));
        }
    }
}
