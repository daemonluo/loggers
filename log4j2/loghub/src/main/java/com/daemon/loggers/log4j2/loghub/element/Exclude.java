package com.daemon.loggers.log4j2.loghub.element;

import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "exclude", category = Node.CATEGORY, printObject = true)
public class Exclude {
    private String value;

    public Exclude(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @PluginFactory
    public static Exclude create(@PluginAttribute("value") final String value) {
        return new Exclude(value);
    }
}
