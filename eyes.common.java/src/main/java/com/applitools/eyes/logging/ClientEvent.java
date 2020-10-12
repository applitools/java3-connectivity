package com.applitools.eyes.logging;

public class ClientEvent {
    private final String timestamp;
    private final String event;
    private final TraceLevel level;

    public ClientEvent(String timestamp, String event) {
        this(timestamp, event, null);
    }

    public ClientEvent(String timestamp, String event, TraceLevel level) {
        this.timestamp = timestamp;
        this.event = event;
        this.level = level;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getEvent() {
        return event;
    }

    public TraceLevel getLevel() {
        return level;
    }
}
