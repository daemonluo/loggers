package com.daemon.loggers;

import java.util.LinkedHashMap;
import java.util.Map;

public class TrackEvent implements Event {
    private final String name;

    private String client;

    private String platform;

    private Map<String, String> properties = new LinkedHashMap<>();

    private long time;

    public TrackEvent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClient() {
        return client;
    }

    public TrackEvent client(String client) {
        this.client = client;
        return this;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    public TrackEvent platform(String platform) {
        this.platform = platform;
        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public TrackEvent properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public TrackEvent property(String key, String value) {
        this.properties.put(key, value);
        return this;
    }

    @Override
    public long getTime() {
        return time;
    }

    public TrackEvent time(long time) {
        this.time = time;
        return this;
    }
}
