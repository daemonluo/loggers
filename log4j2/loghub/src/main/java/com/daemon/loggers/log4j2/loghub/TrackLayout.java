package com.daemon.loggers.log4j2.loghub;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.daemon.loggers.Event;
import com.daemon.loggers.Events;
import com.daemon.loggers.loghub.UnmodifiableTrackEvent;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.layout.AbstractLayout;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Plugin(name = "TrackLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class TrackLayout extends AbstractLayout<Events> {
    /**
     * 其它属性是否展开
     */
    private boolean expand;

    /**
     * 是否保留格式化日志内容
     */
    private boolean reserveFormat;

    /**
     * 是否保留原始日志内容
     */
    private boolean reserveRaw;

    /**
     * 需要抽取的字段
     */
    private Map<String, String> includes;

    /**
     * 需要排除的字段
     */
    private Set<String> excludes;

    TrackLayout(
        Configuration configuration,
        byte[] header,
        byte[] footer,
        boolean expand,
        boolean reserveFormat,
        boolean reserveRaw,
        Map<String, String> includes,
        Set<String> excludes
    ) {
        super(configuration, header, footer);
        this.expand = expand;
        this.reserveFormat = reserveFormat;
        this.reserveRaw = reserveRaw;
        this.includes = includes;
        this.excludes = excludes;
    }

    @Override
    public byte[] toByteArray(LogEvent event) {
        Events events = toSerializable(event);
        JSONArray jsonArray = new JSONArray();
        for (Event e : events.getEvents()) {
            JSONObject jsonObject = new JSONObject(true);
            jsonObject.put("event", e.getName());
            jsonObject.put("client", e.getClient());
            jsonObject.put("platform", e.getPlatform());
            if (isExpand()) {
                for (Map.Entry<String, String> entry : e.getProperties().entrySet()) {
                    jsonObject.put(String.format("properties.%s", entry.getKey()), entry.getValue());
                }
            } else {
                jsonObject.put("properties", e.getProperties());
            }
            jsonObject.put("time", e.getTime());
            jsonArray.add(jsonObject);
        }
        if (events.getTags().isEmpty()) {
            return jsonArray.toJSONString().getBytes();
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("events", jsonArray);
            JSONArray tags = new JSONArray();
            tags.addAll(events.getTags());
            jsonObject.put("tags", tags);
            return jsonObject.toJSONString().getBytes();
        }
    }

    @Override
    public Events toSerializable(LogEvent event) {
        AtomicBoolean hasError = new AtomicBoolean(false);
        List<String> tags = new ArrayList<>();
        List<Event> list = copyFromList(Arrays.asList(event.getMessage().getParameters()), hasError, false, 0);
        if (list.isEmpty()) {
            hasError.set(false);
            list.addAll(copyFromString(event.getMessage().getFormattedMessage(), hasError));
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
        Events events = new Events();
        events.setEvents(list);
        events.setTags(tags);
        return events;
    }

    @Override
    public String getContentType() {
        return null;
    }

    public Map<String, String> getIncludes() {
        return includes;
    }

    public Set<String> getExcludes() {
        return excludes;
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
            if (json.startsWith("{")) {
                Optional<UnmodifiableTrackEvent> optional = UnmodifiableTrackEvent.copyJSON(JSON.parseObject(json));
                if (optional.isPresent()) {
                    events.add(optional.get());
                } else {
                    hasError.set(false);
                }
            } else if (json.startsWith("[")) {
                events.addAll(copyFromList(JSON.parseArray(json), hasError, true, 1));
            } else {
                hasError.set(true);
            }
        } catch (JSONException e) {
            hasError.set(true);
        }
        return events;
    }

    public boolean isExpand() {
        return expand;
    }

    public boolean isReserveFormat() {
        return reserveFormat;
    }

    public boolean isReserveRaw() {
        return reserveRaw;
    }

    @SuppressWarnings("unused")
    @PluginBuilderFactory
    public static <B extends TrackLayoutBuilder<B>> B newBuilder() {
        return new TrackLayoutBuilder<B>().asBuilder();
    }
}
