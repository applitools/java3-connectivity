package com.applitools.eyes;

import com.applitools.utils.EyesSyncObject;

import java.util.concurrent.atomic.AtomicReference;

public class SyncTaskListener<T> implements TaskListener<T> {

    private final Logger logger;
    private final String id;
    private final AtomicReference<EyesSyncObject> syncObject;
    private final AtomicReference<T> reference;


    public SyncTaskListener(Logger logger, String id) {
        this.logger = logger;
        this.id = id;
        this.syncObject = new AtomicReference<>(new EyesSyncObject(logger, id));
        this.reference = new AtomicReference<>();
    }

    @Override
    public void onComplete(T taskResponse) {
        if (logger != null) {
            logger.verbose(String.format("Completed task %s", id));
        }
        reference.set(taskResponse);
        synchronized (syncObject.get()) {
            syncObject.get().notifyObject();
        }
    }

    @Override
    public void onFail() {
        if (logger != null) {
            logger.verbose(String.format("Task %s has failed", id));
        }
        synchronized (syncObject.get()) {
            syncObject.get().notifyObject();
        }
    }

    /**
     * Waits for the task to be completed and then returns the result
     */
    public T get() {
        synchronized (syncObject.get()) {
            try {
                syncObject.get().waitForNotify();
            } catch (InterruptedException e) {
                throw new EyesException("Failed waiting for task", e);
            }
        }
        return reference.get();
    }
}
