package com.daemon.loggers.logback.loghub.element;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.daemon.loggers.Event;
import com.daemon.loggers.Events;
import com.daemon.loggers.loghub.UnmodifiableTrackEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrackLayout extends PatternLayout {
    /**
     * 需要抽取的字段
     */
    private Includes includes;

    /**
     * 需要排除的字段
     */
    private Excludes excludes;

    /**
     * 其它属性是否展开
     */
    private Boolean expand = false;

    /**
     * 是否保留格式化日志内容
     */
    private Boolean reserveFormat = false;

    /**
     * 是否保留原始日志内容
     */
    private Boolean reserveRaw = false;

    public Events toSerializable(ILoggingEvent event) {
        Events events = new Events();
        Object[] args = event.getArgumentArray();
        if (Objects.isNull(args) || args.length == 0) {
            return events;
        }
        AtomicBoolean hasError = new AtomicBoolean(false);
        List<String> tags = new ArrayList<>();
        List<Event> list = copyFromList(Arrays.asList(args), hasError, false, 0);
        if (list.isEmpty()) {
            hasError.set(false);
            list.addAll(copyFromString(event.getFormattedMessage(), hasError));
            if (list.isEmpty()) {
                tags.add(Events.TAG_PARSE_MSG_FAILED);
            } else {
                tags.add(Events.TAG_PARSE_ARGS_FAILED);
            }
        } else {
            if (hasError.get()) {
                tags.add(Events.TAG_PARSE_ARGS_PARTITAL_FAILED);
            }
        }
        events.setEvents(list);
        events.setTags(tags);
        return events;
    }

    @SuppressWarnings("unchecked")
    private List<Event> copyFromList(List<Object> list, AtomicBoolean hasError, boolean skipString, int times) {
        List<Event> events = new ArrayList<>();
        for (Object object : list) {
            if (object == null) {
                continue;
            }
            if (object instanceof Event) {
                Optional<UnmodifiableTrackEvent> optional = UnmodifiableTrackEvent.copyEvent((Event) object);
                if (optional.isPresent()) {
                    events.add(optional.get());
                } else {
                    hasError.set(false);
                }
                continue;
            }
            if (object instanceof Map) {
                Optional<UnmodifiableTrackEvent> optional = UnmodifiableTrackEvent.copyMap((Map<String, String>) object);
                if (optional.isPresent()) {
                    events.add(optional.get());
                } else {
                    hasError.set(false);
                }
                continue;
            }
            if (object instanceof List) {
                events.addAll(copyFromList((List<Object>) object, hasError, true, times + 1));
                continue;
            }
            if (object instanceof String && !skipString) {
                events.addAll(copyFromString((String) object, hasError));
                continue;
            }
            if (times == 0) {
                hasError.set(true);
            }
        }
        return events;
    }

    private List<Event> copyFromString(String json, AtomicBoolean hasError) {
        List<Event> events = new ArrayList<>();
        try {
            if (json.startsWith("{") && json.endsWith("}")) {
                Optional<UnmodifiableTrackEvent> optional = UnmodifiableTrackEvent.copyJSON(JSON.parseObject(json));
                if (optional.isPresent()) {
                    events.add(optional.get());
                } else {
                    hasError.set(false);
                }
            } else if (json.startsWith("[") && json.endsWith("]")) {
                events.addAll(copyFromList(JSON.parseArray(json), hasError, true, 1));
            } else {
                hasError.set(true);
            }
        } catch (JSONException e) {
            hasError.set(true);
        }
        return events;
    }

    public Boolean getExpand() {
        return expand;
    }

    public Boolean getReserveFormat() {
        return reserveFormat;
    }

    public Boolean getReserveRaw() {
        return reserveRaw;
    }

    public Map<String, String> getIncludes() {
        return Optional.ofNullable(includes).map(Includes::getIncludes).orElse(Collections.emptyMap());
    }

    public Set<String> getExcludes() {
        return Optional.ofNullable(excludes).map(Excludes::getExcludes).orElse(Collections.emptySet());
    }

    public void setIncludes(Includes includes) {
        this.includes = includes;
    }

    public void setExcludes(Excludes excludes) {
        this.excludes = excludes;
    }

    public void setExpand(Boolean expand) {
        this.expand = expand;
    }

    public void setReserveFormat(Boolean reserveFormat) {
        this.reserveFormat = reserveFormat;
    }

    public void setReserveRaw(Boolean reserveRaw) {
        this.reserveRaw = reserveRaw;
    }

    public void setConverter(Converter converter) {
        defaultConverterMap.put(converter.getName(), converter.getConverter());
    }
}
