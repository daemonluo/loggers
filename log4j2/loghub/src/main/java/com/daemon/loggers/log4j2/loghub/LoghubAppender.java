package com.daemon.loggers.log4j2.loghub;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.log.common.LogContent;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.util.NetworkUtils;
import com.daemon.loggers.Event;
import com.daemon.loggers.Events;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.util.Strings;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(name = "Loghub", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
final public class LoghubAppender extends AbstractAppender {
    public static final String UUID_KEY = "_uuid_";

    public static final String LOCAL_IP = NetworkUtils.getLocalMachineIP();

    private final ProjectConfig projectConfig;
    private final ProducerConfig producerConfig;

    /**
     * Loghub配置
     */
    private final String project;

    /**
     * Loghub配置
     */
    private final String logstore;

    /**
     * 日志主题
     * 用于分类管理日志
     * 默认用于指代项目
     */
    private final String topic;

    /**
     * 日志生成模块
     * 用于分类管理日志
     * 默认为Logger的名称
     */
    private final String module;

    /**
     * 日志来源
     * 默认为主机IP
     */
    private final String source;

    /**
     * 生成日志的主机
     * 默认为主机名
     */
    private final String host;

    /**
     * 时间格式
     */
    private final DateTimeFormatter formatter;

    /**
     * 时区
     */
    private final ZoneId timezone;

    /**
     * 需要抽取的属性
     */
    private final Map<String, String> includes;

    /**
     * 需要排除的属性
     */
    private final Set<String> excludes;

    private final Map<String, String> predefined;

    private final boolean debug;

    private LogProducer producer;

    LoghubAppender(
        String name,
        Filter filter,
        Layout<? extends Serializable> layout,
        boolean ignoreExceptions,
        String logstore,
        String topic,
        String module,
        String source,
        String host,
        ProjectConfig projectConfig,
        ProducerConfig producerConfig,
        DateTimeFormatter formatter,
        ZoneId timezone,
        LinkedHashMap<String, String> predefined,
        Map<String, String> includes,
        Set<String> excludes,
        boolean debug
    ) {
        super(name, filter, layout, ignoreExceptions);
        this.project = projectConfig.getProject();
        this.logstore = logstore;
        this.topic = topic;
        this.module = module;
        this.source = source;
        this.host = host;
        this.projectConfig = projectConfig;
        this.producerConfig = producerConfig;
        this.formatter = formatter;
        this.timezone = timezone;
        this.predefined = Collections.unmodifiableMap(predefined);
        this.includes = Collections.unmodifiableMap(includes);
        this.excludes = Collections.unmodifiableSet(excludes);
        this.debug = debug;
    }

    @Override
    public void start() {
        super.start();
        producer = new LogProducer(producerConfig);
        producer.putProjectConfig(projectConfig);
    }

    @Override
    protected boolean stop(Future<?> future) {
        boolean result = super.stop(future);
        stopProducer();
        return result;
    }

    @Override
    protected boolean stop(long timeout, TimeUnit timeUnit, boolean changeLifeCycleState) {
        boolean result = super.stop(timeout, timeUnit, changeLifeCycleState);
        stopProducer();
        return result;
    }

    @Override
    public void stop() {
        super.stop();
        stopProducer();
    }

    @Override
    public void append(LogEvent event) {
        List<LogItem> logItems = new ArrayList<>();
        Layout<? extends Serializable> layout = getLayout();
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
        String topic = this.topic;
        String source = this.source;
        String module = this.module;
        for (LogItem item : logItems) {
            if (this.host != null) {
                item.PushBack("_host_", this.host);
            }
            item.PushBack("level", event.getLevel().toString());
            item.PushBack("thread", event.getThreadName());
            StackTraceElement location = event.getSource();
            if (location == null && (!event.isIncludeLocation())) {
                event.setIncludeLocation(true);
                location = event.getSource();
                event.setIncludeLocation(false);
            }

            Optional.ofNullable(event.getThrown()).map(this::getThrowableStr).ifPresent(throwable -> item.PushBack("throwable", throwable));

            item.PushBack("location", Optional.ofNullable(location).map(Object::toString).orElse("Unknown(Unknown Location)"));

            Map<String, String> contextData = event.getContextData().toMap();
            for (Map.Entry<String, String> entry : predefined.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("topic")) {
                    topic = contextData.get(entry.getValue());
                    continue;
                }
                if (entry.getKey().equalsIgnoreCase("source")) {
                    source = contextData.get(entry.getValue());
                    continue;
                }
                if (entry.getKey().equalsIgnoreCase("module")) {
                    module = contextData.get(entry.getValue());
                    continue;
                }
                item.PushBack(entry.getKey(), entry.getValue());
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
            item.PushBack("_module_", StringUtils.isEmpty(module) ? event.getLoggerName() : module);
            if (!containsUUID(item)) {
                item.PushBack(UUID_KEY, UUID.randomUUID().toString());
            }
        }
        topic = StringUtils.isEmpty(topic) ? "" : topic;
        source = StringUtils.isEmpty(source) ? "" : source;
        if (debug) {
            for (LogItem logItem : logItems) {
                System.out.println(logItem.ToJsonString());
            }
        }
        try {
            producer.send(project, logstore, topic, source, logItems, new LoghubAppenderCallback(LOGGER, project, logstore, topic, source, logItems));
        } catch (InterruptedException | ProducerException e) {
            String msg = String.format("Failed to send log, project=%s, logstore=%s, topic=%s, source=%s, logItems=%s: {}", project, logstore, topic, source, logItems.toString());
            LOGGER.error(msg, getThrowableStr(e));
        }
    }

    private List<LogItem> appendCommonEvent(LogEvent event, Layout<? extends Serializable> layout) {
        LogItem item = new LogItem();
        item.SetTime((int) (event.getTimeMillis() / 1000));
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeMillis()), timezone);
        item.PushBack("_time_", dateTime.format(formatter));

        String message = event.getMessage().getFormattedMessage();
        item.PushBack("message", message);

        if (layout != null) {
            item.PushBack("log", new String(layout.toByteArray(event)));
        }

        return Collections.singletonList(item);
    }

    private List<LogItem> appendTrackEvent(LogEvent event, TrackLayout layout) {
        List<LogItem> items = new ArrayList<>();
        Events events = layout.toSerializable(event);
        List<String> tags = events.getTags();
        String jsonTags = tags == null || tags.size() == 0 ? null : new JSONArray().fluentAddAll(tags).toJSONString();
        if (Objects.isNull(events.getTags())) {
            return Collections.emptyList();
        }
        if (events.isEmpty()) {
            final LogItem item = new LogItem();
            item.SetTime((int) (event.getTimeMillis() / 1000));
            item.PushBack("message", event.getMessage().getFormattedMessage());
            if (jsonTags != null) {
                item.PushBack("_tags_", jsonTags);
            }
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeMillis()), timezone);
            item.PushBack("_time_", dateTime.format(formatter));
            item.PushBack("client", LOCAL_IP);
            items.add(item);
        } else {
            for (Event e : events.getEvents()) {
                final LogItem item = new LogItem();
                item.SetTime((int) (event.getTimeMillis() / 1000));
                ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(e.getTime() > 0 ? e.getTime() : event.getTimeMillis()), timezone);
                item.PushBack("_time_", dateTime.format(formatter));
                item.PushBack("event", e.getName());
                item.PushBack("client", Strings.isBlank(e.getClient()) ? NetworkUtils.getLocalMachineIP() : e.getClient());
                if (Strings.isNotBlank(e.getPlatform())) {
                    item.PushBack("platform", e.getPlatform());
                }
                Map<String, String> properties = e.getProperties();
                final Map<String, String> remain = new LinkedHashMap<>();
                final Set<String> excludes = layout.getExcludes();
                final Map<String, String> includes = layout.getIncludes();
                if (properties != null && properties.size() > 0) {
                    if (layout.isExpand()) {
                        properties.forEach((key, value) -> {
                            if (Strings.isEmpty(value)) {
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
                            if (Strings.isEmpty(value)) {
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
                if (layout.isReserveRaw()) {
                    item.PushBack("message", event.getMessage().getFormattedMessage());
                }
                if (layout.isReserveFormat()) {
                    item.PushBack("log", new String(layout.toByteArray(event)));
                }
                items.add(item);
            }
        }
        return items;
    }

    private String getThrowableStr(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String s : Throwables.toStringList(throwable)) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private boolean containsUUID(LogItem item) {
        for (LogContent content : item.GetLogContents()) {
            if (content.GetKey().equalsIgnoreCase(UUID_KEY)) {
                return Strings.isNotEmpty(content.GetValue());
            }
        }
        return false;
    }

    private void stopProducer() {
        if (debug) {
            System.out.println("close loghub producer");
        }
        if (Objects.isNull(producer)) {
            return;
        }
        try {
            producer.close();
        } catch (InterruptedException | ProducerException e) {
            LOGGER.error("producer close error: {}", getThrowableStr(e));
        }
    }

    @SuppressWarnings("unused")
    @PluginBuilderFactory
    public static <B extends LoghubAppenderBuilder<B>> B newBuilder() {
        return new LoghubAppenderBuilder<B>().asBuilder();
    }
}
