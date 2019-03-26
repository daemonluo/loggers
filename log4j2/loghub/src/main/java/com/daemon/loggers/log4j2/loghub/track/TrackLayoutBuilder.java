package com.daemon.loggers.log4j2.loghub.track;

import com.daemon.loggers.log4j2.loghub.LoghubAppender;
import com.daemon.loggers.log4j2.loghub.element.Columns;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractLayout;
import org.apache.logging.log4j.core.util.Builder;

import java.util.*;

public class TrackLayoutBuilder<B extends TrackLayoutBuilder<B>> extends AbstractLayout.Builder<B> implements Builder<TrackLayout> {
    /**
     * UUID字段
     */
    @PluginBuilderAttribute("uuid")
    private String uuid = LoghubAppender.UUID_KEY;

    /**
     * 其它属性是否展开
     */
    @PluginBuilderAttribute("expand")
    private boolean expand = false;

    /**
     * 是否保留格式化日志内容
     */
    @PluginBuilderAttribute("format")
    private boolean reserveFormat = false;

    /**
     * 是否保留原始日志内容
     */
    @PluginBuilderAttribute("raw")
    private boolean reserveRaw = false;

    @PluginElement("columns")
    private Columns columns;

    public String getUuid() {
        return uuid;
    }

    public B setUuid(String uuid) {
        this.uuid = uuid;
        return asBuilder();
    }

    public boolean isExpand() {
        return expand;
    }

    public B setExpand(boolean expand) {
        this.expand = expand;
        return asBuilder();
    }

    public boolean isReserveFormat() {
        return reserveFormat;
    }

    public B setReserveFormat(boolean reserveFormat) {
        this.reserveFormat = reserveFormat;
        return asBuilder();
    }

    public boolean isReserveRaw() {
        return reserveRaw;
    }

    public B setReserveRaw(boolean reserveRaw) {
        this.reserveRaw = reserveRaw;
        return asBuilder();
    }

    public Columns getColumns() {
        return columns;
    }

    public B setColumns(Columns columns) {
        this.columns = columns;
        return asBuilder();
    }

    @Override
    public TrackLayout build() {
        Columns columns = getColumns();
        Map<String, String> includes = columns == null ? new HashMap<>() : new HashMap<>(columns.getIncludes());
        Set<String> excludes = columns == null ? new HashSet<>() : columns.getExcludes();
        includes.put(getUuid() == null ? LoghubAppender.UUID_KEY : getUuid(), LoghubAppender.UUID_KEY);
        if (getUuid() != null) {
            includes.put(getUuid(), LoghubAppender.UUID_KEY);
        } else {
            includes.put(LoghubAppender.UUID_KEY, LoghubAppender.UUID_KEY);
        }
        return new TrackLayout(
            getConfiguration(),
            getHeader(),
            getFooter(),
            isExpand(),
            isReserveFormat(),
            isReserveRaw(),
            Collections.unmodifiableMap(includes),
            excludes
        );
    }
}
