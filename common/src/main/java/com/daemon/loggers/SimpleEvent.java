package com.daemon.loggers;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleEvent implements Event {
    private final String name;

    private String client;

    private String platform;

    private Map<String, String> properties = new LinkedHashMap<>();

    private long time;

    public SimpleEvent(String name) {
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

    public SimpleEvent client(String client) {
        this.client = client;
        return this;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    public SimpleEvent platform(String platform) {
        this.platform = platform;
        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public SimpleEvent properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public SimpleEvent property(String key, String value) {
        this.properties.put(key, value);
        return this;
    }

    @Override
    public long getTime() {
        return time;
    }

    public SimpleEvent time(long time) {
        this.time = time;
        return this;
    }
}
