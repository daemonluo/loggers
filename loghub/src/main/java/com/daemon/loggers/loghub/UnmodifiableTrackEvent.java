package com.daemon.loggers.loghub;

import com.alibaba.fastjson.JSONObject;
import com.daemon.loggers.Event;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class UnmodifiableTrackEvent implements Event {
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

    public static Optional<UnmodifiableTrackEvent> copyEvent(Event event) {
        String name = event.getName();
        if (StringUtils.isBlank(name)) {
            return Optional.empty();
        }
        final UnmodifiableTrackEvent clone = new UnmodifiableTrackEvent();
        clone.setName(name);
        clone.setClient(event.getClient());
        clone.setPlatform(event.getPlatform());
        clone.setProperties(Optional.ofNullable(event.getProperties()).orElse(Collections.emptyMap()));
        clone.setTime(event.getTime());
        return Optional.of(clone);
    }

    public static Optional<UnmodifiableTrackEvent> copyMap(Map<String, String> map) {
        Map<String, String> clone = new HashMap<>(map);
        String name = clone.remove("event");
        if (StringUtils.isEmpty(name)) {
            return Optional.empty();
        }
        final UnmodifiableTrackEvent event = new UnmodifiableTrackEvent();
        event.setName(name);
        Optional.ofNullable(clone.remove("client")).ifPresent(event::setClient);
        Optional.ofNullable(clone.remove("platform")).ifPresent(event::setPlatform);
        Optional.ofNullable(clone.remove("time"))
            .map(l -> {
                try {
                    return Long.parseLong(l);
                } catch (NumberFormatException e) {
                    return null;
                }
            })
            .ifPresent(event::setTime);
        if (event.getTime() < 0) {
            return Optional.empty();
        }
        event.setProperties(clone);
        return Optional.of(event);
    }

    public static Optional<UnmodifiableTrackEvent> copyJSON(JSONObject jsonObject) {
        Object name = jsonObject.get("event");
        if (Objects.isNull(name) || StringUtils.isBlank(name.toString())) {
            return Optional.empty();
        }
        Map<String, String> map = new HashMap<>(jsonObject.size());
        for (String key : jsonObject.keySet()) {
            Object object = jsonObject.get(key);
            if (Objects.isNull(object)) {
                continue;
            }
            map.put(key, object.toString());
        }
        final UnmodifiableTrackEvent event = new UnmodifiableTrackEvent();
        event.setName(map.remove("event"));
        Optional.ofNullable(map.remove("client")).ifPresent(event::setClient);
        Optional.ofNullable(map.remove("platform")).ifPresent(event::setPlatform);
        Optional.ofNullable(map.remove("time"))
            .map(l -> {
                try {
                    return Long.parseLong(l);
                } catch (NumberFormatException e) {
                    return null;
                }
            })
            .ifPresent(event::setTime);
        if (event.getTime() < 0) {
            return Optional.empty();
        }
        event.setProperties(map);
        return Optional.of(event);
    }
}
