package com.daemon.loggers.log4j2.loghub.track;

import com.alibaba.fastjson.JSONObject;
import com.daemon.loggers.Event;

import java.util.Map;

public class TrackEvent implements Event {
    private String name;
    private String client;
    private String platform;
    private Map<String, String> properties;
    private long time;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.put("name", getName());
        if (getClient() != null) {
            jsonObject.put("client", getClient());
        }
        if (getPlatform() != null) {
            jsonObject.put("platform", getPlatform());
        }
        if (getProperties() != null) {
            JSONObject properties = new JSONObject(getProperties().size(), true);
            properties.putAll(getProperties());
            jsonObject.put("properties", properties);
        }
        jsonObject.put("time", getTime());
        return jsonObject.toJSONString();
    }
}
