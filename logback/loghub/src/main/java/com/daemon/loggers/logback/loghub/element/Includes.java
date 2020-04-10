package com.daemon.loggers.logback.loghub.element;

import java.util.HashMap;
import java.util.Map;

public class Includes {
    private Map<String, String> includes = new HashMap<>();

    public void setPair(Pair pair) {
        includes.put(pair.getKey(), pair.getValue());
    }

    public Map<String, String> getIncludes() {
        return includes;
    }
}
