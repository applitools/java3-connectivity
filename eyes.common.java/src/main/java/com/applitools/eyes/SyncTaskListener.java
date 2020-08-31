package com.applitools.eyes;

import com.applitools.utils.EyesSyncObject;

import java.util.concurrent.atomic.AtomicReference;

public class SyncTaskListener<T> implements TaskListener<T> {

    private final AtomicReference<EyesSyncObject> syncObject;
    private final AtomicReference<T> reference;


    public SyncTaskListener(AtomicReference<EyesSyncObject> syncObject) {
        this(syncObject, null);
    }

    public SyncTaskListener(AtomicReference<EyesSyncObject> syncObject, AtomicReference<T> reference) {
        this.syncObject = syncObject;
        this.reference = reference;
    }

    @Override
    public void onComplete(T taskResponse) {
        if (reference != null) {
            reference.set(taskResponse);
        }

        synchronized (syncObject.get()) {
            syncObject.get().notifyObject();
        }
    }

    @Override
    public void onFail() {
        synchronized (syncObject.get()) {
            syncObject.get().notifyObject();
        }
    }

    public AtomicReference<T> getReference() {
        return reference;
    }
}
