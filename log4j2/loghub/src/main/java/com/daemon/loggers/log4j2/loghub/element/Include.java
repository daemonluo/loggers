package com.daemon.loggers.log4j2.loghub.element;

import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "include", category = Node.CATEGORY, printObject = true)
public class Include {
    private String value;

    private String dest;

    public Include(String value, String dest) {
        this.value = value;
        this.dest = dest;
    }

    public String getValue() {
        return value;
    }

    public String getDest() {
        return dest;
    }

    @PluginFactory
    public static Include create(@PluginAttribute("value") final String value, @PluginAttribute("dest") final String dest) {
        return new Include(value, dest);
    }
}
