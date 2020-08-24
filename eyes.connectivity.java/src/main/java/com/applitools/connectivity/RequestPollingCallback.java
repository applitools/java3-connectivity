package com.applitools.connectivity;

import com.applitools.connectivity.api.AsyncRequestCallback;
import com.applitools.connectivity.api.Response;
import com.applitools.eyes.EyesException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import javax.ws.rs.HttpMethod;

/**
 * Callback used for sending long requests to the server
 */
class RequestPollingCallback implements AsyncRequestCallback {

    private final RestClient restClient;
    private final String pollingUrl;
    private final AsyncRequestCallback pollingFinishedCallback;
    private int sleepDuration = 500;

    RequestPollingCallback(RestClient restClient, String pollingUrl, AsyncRequestCallback pollingFinishedCallback) {
        this.restClient = restClient;
        this.pollingUrl = pollingUrl;
        this.pollingFinishedCallback = pollingFinishedCallback;
    }

    @Override
    public void onComplete(Response response) {
        try {
            int status = response.getStatusCode();
            if (status == HttpStatus.SC_CREATED) {
                restClient.logger.verbose("exit (CREATED)");
                restClient.sendAsyncRequest(pollingFinishedCallback, response.getHeader(HttpHeaders.LOCATION, false), HttpMethod.DELETE);
                return;
            }

            if (status != HttpStatus.SC_OK) {
                pollingFinishedCallback.onFail(new EyesException(
                        String.format("Got bad status code when polling from the server. Status code: %d", status)));
                return;
            }

            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                pollingFinishedCallback.onFail(new EyesException("Long request interrupted!", e));
                return;
            }
        } finally {
            response.close();
        }

        sleepDuration *= 2;
        sleepDuration = Math.min(10000, sleepDuration);
        restClient.logger.verbose("polling...");
        restClient.sendAsyncRequest(this, pollingUrl, HttpMethod.GET);
    }

    @Override
    public void onFail(Throwable throwable) {
        pollingFinishedCallback.onFail(throwable);
    }
}
