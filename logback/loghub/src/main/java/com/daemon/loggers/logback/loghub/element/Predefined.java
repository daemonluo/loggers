package com.daemon.loggers.logback.loghub.element;

import java.util.HashMap;
import java.util.Map;

public class Predefined {
    private Map<String, String> predefined = new HashMap<>();

    public void setPair(Pair pair) {
        predefined.put(pair.getKey(), pair.getValue());
    }

    public Map<String, String> getPredefined() {
        return predefined;
    }
}
