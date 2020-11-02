package com.applitools.eyes;

import com.applitools.utils.EyesSyncObject;

public class SyncTaskListener<T> implements TaskListener<T> {

    private final Logger logger;
    private String id;
    private final EyesSyncObject syncObject;
    private T reference = null;

    public SyncTaskListener(Logger logger) {
        this(logger, "");
    }

    public SyncTaskListener(Logger logger, String id) {
        this.logger = logger;
        this.id = id;
        this.syncObject = new EyesSyncObject(logger, id);
    }

    public void setId(String id) {
        this.id = id;
        this.syncObject.setId(id);
    }

    @Override
    public void onComplete(T taskResponse) {
        if (logger != null) {
            logger.verbose(String.format("Completed task %s", id));
        }

        synchronized (syncObject) {
            reference = taskResponse;
            syncObject.notifyObject();
        }
    }

    @Override
    public void onFail() {
        if (logger != null) {
            logger.verbose(String.format("Task %s has failed", id));
        }

        synchronized (syncObject) {
            syncObject.notifyObject();
        }
    }

    /**
     * Waits for the task to be completed and then returns the result
     */
    public T get() {
        synchronized (syncObject) {
            if (syncObject.isNotified()) {
                return reference;
            }
            try {
                syncObject.waitForNotify();
            } catch (InterruptedException e) {
                throw new EyesException("Failed waiting for task", e);
            }
            return reference;
        }
    }
}
