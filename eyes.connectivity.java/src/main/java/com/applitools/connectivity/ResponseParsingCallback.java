package com.applitools.connectivity;

import com.applitools.connectivity.api.AbstractAsyncCallback;
import com.applitools.connectivity.api.Response;
import com.applitools.eyes.TaskListener;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class ResponseParsingCallback<T> extends AbstractAsyncCallback<T> {

    private final RestClient restClient;
    private final List<Integer> validStatusCodes;

    public ResponseParsingCallback(RestClient restClient, List<Integer> validStatusCodes, TaskListener<T> listener) {
        super(restClient.logger, listener);
        this.restClient = restClient;
        this.validStatusCodes = validStatusCodes;
    }

    @Override
    public T onCompleteInner(Response response) {
        return restClient.parseResponseWithJsonData(response, validStatusCodes, new TypeReference<T>() {});
    }
}
