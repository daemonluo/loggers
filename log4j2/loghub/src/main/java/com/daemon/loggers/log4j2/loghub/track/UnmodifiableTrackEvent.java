package com.daemon.loggers.log4j2.loghub.track;

import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.stream.Collectors;

final class UnmodifiableTrackEvent implements Event {
    private String name;
    private String client;
    private String platform;
    private Map<String, String> properties;
    private long time;

    @Override
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @Override
    public String getClient() {
        return client;
    }

    private void setClient(String client) {
        this.client = client;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    private void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    private void setProperties(Map<String, String> properties) {
        this.properties = Collections.unmodifiableMap(properties);
    }

    @Override
    public long getTime() {
        return time;
    }

    private void setTime(long time) {
        this.time = time;
    }

    static Optional<UnmodifiableTrackEvent> copyEvent(Event event) {
        String name = event.getName();
        if (Strings.isBlank(name)) {
            return Optional.empty();
        }
        final UnmodifiableTrackEvent clone = new UnmodifiableTrackEvent();
        clone.setName(name);
        clone.setClient(event.getClient());
        clone.setPlatform(event.getPlatform());
        clone.setProperties(event.getProperties() == null ? new HashMap<>() : event.getProperties());
        clone.setTime(event.getTime());
        return Optional.of(clone);
    }

    static Optional<UnmodifiableTrackEvent> copyMap(Map<String, String> map) {
        String name = map.get("event");
        if (Strings.isEmpty(name)) {
            return Optional.empty();
        }
        final UnmodifiableTrackEvent event = new UnmodifiableTrackEvent();
        event.setName(name);
        event.setClient(map.get("client"));
        event.setPlatform(map.get("platform"));
        String time = map.get("time");
        if (time != null) {
            try {
                event.setTime(Long.valueOf(time));
            } catch (NumberFormatException ignored) { }
        }
        event.setProperties(map.entrySet().stream()
            .filter(entry -> Strings.isNotEmpty(entry.getValue()))
            .filter(entry -> !entry.getKey().equals("event"))
            .filter(entry -> !entry.getKey().equals("client"))
            .filter(entry -> !entry.getKey().equals("platform"))
            .filter(entry -> !entry.getKey().equals("time") || event.getTime() <= 0)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return Optional.of(event);
    }

    static Optional<UnmodifiableTrackEvent> copyJSON(JSONObject jsonObject) {
        Object name = jsonObject.get("event");
        if (Objects.isNull(name) || Strings.isEmpty(name.toString())) {
            return Optional.empty();
        }
        final UnmodifiableTrackEvent event = new UnmodifiableTrackEvent();
        event.setName(name.toString());
        Object client = jsonObject.get("client");
        if (client != null && Strings.isNotBlank(client.toString())) {
            event.setClient(client.toString());
        }
        Object platform = jsonObject.get("platform");
        if (platform != null && Strings.isNotBlank(platform.toString())) {
            event.setPlatform(platform.toString());
        }
        try {
            Long time = jsonObject.getLong("time");
            if (time != null) {
                event.setTime(time);
            }
        } catch (NumberFormatException ignored) { }
        event.setProperties(jsonObject.entrySet().stream()
            .filter(entry -> Objects.nonNull(entry.getValue()) && Strings.isNotEmpty(entry.getValue().toString()))
            .filter(entry -> !entry.getKey().equals("event"))
            .filter(entry -> !entry.getKey().equals("client"))
            .filter(entry -> !entry.getKey().equals("platform"))
            .filter(entry -> !entry.getKey().equals("time") || event.getTime() <= 0)
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString())));
        return Optional.of(event);
    }
}
