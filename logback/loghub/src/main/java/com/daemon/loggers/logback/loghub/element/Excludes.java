package com.daemon.loggers.logback.loghub.element;

import java.util.HashSet;
import java.util.Set;

public class Excludes {
    private Set<String> excludes = new HashSet<>();

    public void setExclude(String exclude) {
        excludes.add(exclude);
    }

    public Set<String> getExcludes() {
        return excludes;
    }
}
