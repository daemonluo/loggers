package com.daemon.loggers.logback.loghub.element;

import java.util.Optional;

public class Pair {
    private String key;

    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return Optional.ofNullable(value).orElse(key);
    }

    public void setValue(String value) {
        this.value = value;
    }
}
