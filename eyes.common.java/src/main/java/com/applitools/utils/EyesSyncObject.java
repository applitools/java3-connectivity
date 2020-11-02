package com.applitools.utils;

import com.applitools.eyes.Logger;

public class EyesSyncObject {

    private static final int WAIT_TIMEOUT = 60 * 1000;

    private boolean isNotified = false;
    private final Logger logger;
    private String id;
    private int timeWaited = 0;


    public EyesSyncObject(Logger logger, String id) {
        this.logger = logger;
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void waitForNotify() throws InterruptedException {
        try {
            if (isNotified) {
                return;
            }

            while (true) {
                wait(WAIT_TIMEOUT);
                if (isNotified) {
                    return;
                }

                timeWaited += WAIT_TIMEOUT;
                String message = String.format("WARNING: Waiting for %dms on object %s", timeWaited, id);
                if (logger != null) {
                    logger.log(message);
                } else {
                    System.out.println(message);
                }
            }
        } finally {
            isNotified = false;
        }
    }

    public void notifyObject() {
        isNotified = true;
        notify();
    }
}
