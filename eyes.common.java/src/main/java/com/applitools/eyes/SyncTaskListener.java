package com.applitools.eyes;

import java.util.concurrent.atomic.AtomicReference;

public class SyncTaskListener<T> implements TaskListener<T> {

    private final Object syncObject;
    private final AtomicReference<T> reference;


    public SyncTaskListener(Object syncObject) {
        this(syncObject, null);
    }

    public SyncTaskListener(Object syncObject, AtomicReference<T> reference) {
        this.syncObject = syncObject;
        this.reference = reference;
    }

    @Override
    public void onComplete(T taskResponse) {
        if (reference != null) {
            reference.set(taskResponse);
        }

        synchronized (syncObject) {
            syncObject.notify();
        }
    }

    @Override
    public void onFail() {
        synchronized (syncObject) {
            syncObject.notify();
        }
    }
}
