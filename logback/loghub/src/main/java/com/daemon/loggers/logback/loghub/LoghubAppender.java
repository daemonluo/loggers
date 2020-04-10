package com.daemon.loggers.logback.loghub;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.log.common.LogContent;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.util.NetworkUtils;
import com.daemon.loggers.Event;
import com.daemon.loggers.Events;
import com.daemon.loggers.logback.loghub.element.*;
import org.apache.commons.lang.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class LoghubAppender extends AppenderBase<ILoggingEvent> {
    public static final String UUID_KEY = "_uuid_";

    public static final String LOCAL_IP = NetworkUtils.getLocalMachineIP();

    private Auth auth;

    private Destination destination;

    private Policy policy;

    private Excludes excludes;

    private Includes includes;

    private Predefined predefined;

    private LayoutWrappingEncoder<ILoggingEvent> encoder;

    private Boolean debug = Boolean.FALSE;

    private transient DateTimeFormatter formatter;

    private transient ZoneId zoneId;

    private transient Producer producer;

    @Override
    public void start() {
        try {
            ProjectConfig projectConfig = auth.buildProjectConfig();
            ProducerConfig producerConfig = policy.buildProducerConfig();
            formatter = policy.getFormatter();
            zoneId = policy.getTimeZone();
            producer = new LogProducer(producerConfig);
            producer.putProjectConfig(projectConfig);
            super.start();
        } catch (Exception e) { addError("Failed to start LoghubAppender.", e);
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        List<LogItem> logItems = new ArrayList<>();
        Layout<ILoggingEvent> layout = Optional.ofNullable(encoder)
            .map(LayoutWrappingEncoder::getLayout)
            .orElse(null);
        if (layout == null) {
            logItems.addAll(appendCommonEvent(event, null));
        } else {
            if (layout instanceof TrackLayout) {
                logItems.addAll(appendTrackEvent(event, (TrackLayout) layout));
            } else {
                logItems.addAll(appendCommonEvent(event, layout));
            }
        }
        if (logItems.isEmpty()) {
            return;
        }
        final String project = auth.getProject();
        final String logstore = auth.getLogstore();
        final String host = destination.getHost();
        final Map<String, String> includes = Optional.ofNullable(this.includes).map(Includes::getIncludes).orElse(Collections.emptyMap());
        final Set<String> excludes = Optional.ofNullable(this.excludes).map(Excludes::getExcludes).orElse(Collections.emptySet());
        final Map<String, String> predefined = Optional.ofNullable(this.predefined).map(Predefined::getPredefined).orElse(Collections.emptyMap());
        String topic = destination.getTopic();
        String source = destination.getSource();
        String module = destination.getModule();
        for (LogItem item : logItems) {
            if (host != null) {
                item.PushBack("_host_", host);
            }
            item.PushBack("level", event.getLevel().toString());
            item.PushBack("thread", event.getThreadName());
            StackTraceElement[] stacks = event.getCallerData();
            if (Objects.nonNull(stacks) && stacks.length > 0) {
                item.PushBack("location", stacks[0].toString());
            } else {
                item.PushBack("location", "Unknown(Unknown Location)");
            }

            getThrowableStr(event.getThrowableProxy()).ifPresent(throwable -> item.PushBack("throwable", throwable));

            Map<String, String> contextData = event.getMDCPropertyMap();
            for (Map.Entry<String, String> entry : predefined.entrySet()) {
                String key = StringUtils.trimToNull(entry.getKey());
                if (Objects.isNull(key)) {
                    continue;
                }
                if (key.equalsIgnoreCase("topic")) {
                    topic = contextData.get(entry.getValue());
                    continue;
                }
                if (key.equalsIgnoreCase("source")) {
                    source = contextData.get(entry.getValue());
                    continue;
                }
                if (key.equalsIgnoreCase("module")) {
                    module = contextData.get(entry.getValue());
                    continue;
                }
                item.PushBack(key, entry.getValue());
            }
            if (contextData.size() > 0) {
                Map<String, String> properties = new LinkedHashMap<>();
                String[] keys = contextData.keySet().toArray(new String[0]);
                Arrays.sort(keys);
                for (String key : keys) {
                    if (key.equalsIgnoreCase("topic")) {
                        topic = contextData.get(key);
                        continue;
                    }
                    if (key.equalsIgnoreCase("source")) {
                        source = contextData.get(key);
                        continue;
                    }
                    if (key.equalsIgnoreCase("module")) {
                        module = contextData.get(key);
                        continue;
                    }
                    properties.put(key, contextData.get(key));
                }
                if (!includes.isEmpty()) {
                    properties = properties.entrySet().stream()
                        .filter(e -> Objects.nonNull(includes.get(e.getKey())))
                        .collect(Collectors.toMap(e -> includes.get(e.getKey()), Map.Entry::getValue));
                }
                if (!excludes.isEmpty()) {
                    properties = properties.entrySet().stream()
                        .filter(e -> !excludes.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }
                if (!properties.isEmpty()) {
                    JSONObject json = new JSONObject(properties.size(), true);
                    properties.forEach(json::put);
                    item.PushBack("properties", json.toJSONString());
                }
            }
            item.PushBack("_module_", Optional.ofNullable(module).map(StringUtils::trimToNull).orElseGet(event::getLoggerName));
            if (!containsUUID(item)) {
                item.PushBack(UUID_KEY, UUID.randomUUID().toString());
            }
        }
        topic = StringUtils.isBlank(topic) ? "" : topic;
        source = StringUtils.isBlank(source) ? "" : source;
        if (debug) {
            for (LogItem logItem : logItems) {
                System.out.println(logItem.ToJsonString());
            }
        }
        try {
            producer.send(project, logstore, topic, source, logItems, new LoghubAppenderCallback(this, project, logstore, topic, source, logItems));
        } catch (InterruptedException | ProducerException e) {
            String msg = String.format("Failed to send log, project=%s, logstore=%s, topic=%s, source=%s, logItems=%s", project, logstore, topic, source, logItems.toString());
            addError(msg, e);
        }
    }


    private List<LogItem> appendCommonEvent(ILoggingEvent event, Layout<ILoggingEvent> layout) {
        LogItem item = new LogItem();
        item.SetTime((int) (event.getTimeStamp() / 1000));
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), zoneId);
        item.PushBack("_time_", dateTime.format(formatter));

        String message = event.getFormattedMessage();
        item.PushBack("message", message);

        if (Objects.nonNull(layout)) {
            Optional.ofNullable(layout.doLayout(event)).ifPresent(log -> item.PushBack("log", log));
        } else {
            item.PushBack("log", message);
        }

        return Collections.singletonList(item);
    }

    private List<LogItem> appendTrackEvent(ILoggingEvent event, TrackLayout layout) {
        List<LogItem> items = new ArrayList<>();
        Events events = layout.toSerializable(event);
        if (Objects.isNull(events.getTags())) {
            return Collections.emptyList();
        }
        List<String> tags = events.getTags();
        String jsonTags = tags == null || tags.size() == 0 ? null : new JSONArray().fluentAddAll(tags).toJSONString();
        if (events.isEmpty()) {
            final LogItem item = new LogItem();
            item.SetTime((int) (event.getTimeStamp() / 1000));
            item.PushBack("message", event.getFormattedMessage());
            if (jsonTags != null) {
                item.PushBack("_tags_", jsonTags);
            }
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), zoneId);
            item.PushBack("_time_", dateTime.format(formatter));
            item.PushBack("client", LOCAL_IP);
            items.add(item);
        } else {
            for (Event e : events.getEvents()) {
                final LogItem item = new LogItem();
                item.SetTime((int) (event.getTimeStamp() / 1000));
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(e.getTime() > 0 ? e.getTime() : event.getTimeStamp()), zoneId);
                item.PushBack("_time_", dateTime.format(formatter));
                item.PushBack("event", e.getName());
                item.PushBack("client", Optional.ofNullable(e.getClient()).orElse(LOCAL_IP));

                if (StringUtils.isNotBlank(e.getPlatform())) {
                    item.PushBack("platform", e.getPlatform());
                }
                Map<String, String> properties = e.getProperties();
                final Map<String, String> remain = new LinkedHashMap<>();
                final Set<String> excludes = layout.getExcludes();
                final Map<String, String> includes = layout.getIncludes();
                if (properties != null && properties.size() > 0) {
                    if (layout.getExpand()) {
                        properties.forEach((key, value) -> {
                            if (StringUtils.isEmpty(value)) {
                                return;
                            }
                            if (excludes.contains(key)) {
                                remain.put(key, value);
                            } else {
                                item.PushBack(includes.getOrDefault(key, key), value);
                            }
                        });
                    } else {
                        properties.forEach((key, value) -> {
                            if (StringUtils.isEmpty(value)) {
                                return;
                            }
                            if (Objects.nonNull(includes.get(key))) {
                                item.PushBack(includes.get(key), value);
                            } else {
                                remain.put(key, value);
                            }
                        });
                    }
                }
                if (!remain.isEmpty()) {
                    item.PushBack("data", new JSONObject(remain.size(), true).fluentPutAll(remain).toJSONString());
                }
                if (jsonTags != null) {
                    item.PushBack("_tags_", jsonTags);
                }
                if (layout.getReserveRaw() || (tags != null && tags.size() > 0)) {
                    item.PushBack("message", event.getFormattedMessage());
                }
                if (layout.getReserveFormat()) {
                    item.PushBack("log", layout.doLayout(event));
                }
                items.add(item);
            }
        }
        return items;
    }

    private Optional<String> getThrowableStr(IThrowableProxy proxy) {
        if (Objects.isNull(proxy)) {
            return Optional.empty();
        }
        String message = proxy.getMessage();
        StringBuilder builder = new StringBuilder(proxy.getClassName());
        if (Objects.nonNull(message)) {
            builder.append(": ")
                .append(message);
        }
        for (StackTraceElementProxy step : proxy.getStackTraceElementProxyArray()) {
            builder.append(CoreConstants.LINE_SEPARATOR);
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
        }
        return Optional.of(builder.toString());
    }

    private boolean containsUUID(LogItem item) {
        for (LogContent content : item.GetLogContents()) {
            if (content.GetKey().equalsIgnoreCase(UUID_KEY)) {
                return StringUtils.isNotEmpty(content.GetValue());
            }
        }
        return false;
    }

    @Override
    public void stop() {
        if (debug) {
            System.out.println("close loghub producer");
        }
        try {
            if (!isStarted()) {
                return;
            }
            super.stop();
            producer.close();
        } catch (Exception e) {
            addError("Failed to stop LoghubAppender.", e);
        }
    }

    /* ================= set property =================== */
    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public void setExcludes(Excludes excludes) {
        this.excludes = excludes;
    }

    public void setIncludes(Includes includes) {
        this.includes = includes;
    }

    public void setPredefined(Predefined predefined) {
        this.predefined = predefined;
    }

    public void setEncoder(LayoutWrappingEncoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }
}
