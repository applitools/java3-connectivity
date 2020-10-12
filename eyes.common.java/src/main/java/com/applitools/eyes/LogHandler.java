package com.applitools.eyes;

import com.applitools.eyes.logging.TraceLevel;

/**
 * Handles log messages produces by the Eyes API.
 */
public abstract class LogHandler {
    private final boolean isVerbose;

    protected LogHandler(boolean isVerbose) {
        this.isVerbose = isVerbose;
    }

    public abstract void open();

    public void onMessage(TraceLevel level, String message) {
        if (level == null) {
            level = TraceLevel.Notice;
        }
        if (level.isHigherThan(TraceLevel.Notice) || isVerbose) {
            onMessage(String.format("[%s]\t%s", level.name(), message));
        }
    }

    public abstract void onMessage(String message);

    public abstract void close();

    public abstract boolean isOpen();
}
