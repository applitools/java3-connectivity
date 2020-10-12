package com.applitools.eyes;

import com.applitools.eyes.logging.TraceLevel;

/**
 * Ignores all log messages.
 */
public class NullLogHandler extends LogHandler {

    public static final NullLogHandler instance = new NullLogHandler();

    protected NullLogHandler() {
        super(false);
    }

    public void onMessage(String message) {
    }

    public void open() {
    }

    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }
}