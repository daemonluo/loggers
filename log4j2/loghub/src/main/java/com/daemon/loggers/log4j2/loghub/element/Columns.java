package com.daemon.loggers.log4j2.loghub.element;

import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.stream.Collectors;

@Plugin(name = "columns", category = Node.CATEGORY, printObject = true)
public class Columns {
    private Map<String, String> includes;

    private Set<String> excludes;

    private Columns(Map<String, String> includes, Set<String> excludes) {
        this.includes = Collections.unmodifiableMap(includes);
        this.excludes = Collections.unmodifiableSet(excludes);
    }

    public Map<String, String> getIncludes() {
        return includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    private static Map<String, String> createIncludeMap(final Include[] includes) {
        if (Objects.isNull(includes)) {
            return Collections.emptyMap();
        } else {
            return Arrays.stream(includes).collect(Collectors.toMap(Include::getValue, include -> Optional.ofNullable(include.getDest()).orElse(include.getValue())));
        }
    }

    private static Set<String> createExcludeSet(final Exclude[] excludes) {
        if (Objects.isNull(excludes)) {
            return Collections.emptySet();
        } else {
            return Arrays.stream(excludes).map(Exclude::getValue).filter(Strings::isNotBlank).collect(Collectors.toSet());
        }
    }

    @PluginFactory
    public static Columns createColumns(@PluginElement("include") final Include[] includes, @PluginElement("exclude") final Exclude[] excludes) {
        return new Columns(createIncludeMap(includes), createExcludeSet(excludes));
    }
}
