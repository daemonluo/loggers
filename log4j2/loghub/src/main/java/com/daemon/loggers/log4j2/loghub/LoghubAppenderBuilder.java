package com.daemon.loggers.log4j2.loghub;

import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.daemon.loggers.log4j2.loghub.element.Columns;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.util.Builder;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.util.Strings;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("unused")
public class LoghubAppenderBuilder<B extends LoghubAppenderBuilder<B>> extends AbstractAppender.Builder<B> implements Builder<LoghubAppender> {
    @PluginBuilderAttribute
    @Required
    private String project;

    @PluginBuilderAttribute
    @Required
    private String logstore;

    @PluginBuilderAttribute
    @Required
    private String endpoint;

    @PluginBuilderAttribute
    @Required
    private String accessId;

    @PluginBuilderAttribute
    @Required
    private String accessKey;

    @PluginBuilderAttribute
    private String stsToken;

    @PluginBuilderAttribute
    private String userAgent = "log4j2-loghub-appender";

    @PluginBuilderAttribute("topic")
    private String topic;

    @PluginBuilderAttribute("module")
    private String module;

    @PluginBuilderAttribute("source")
    private String source;

    @PluginBuilderAttribute("host")
    private String host;

    @PluginBuilderAttribute("timeout")
    private int timeout;

    @PluginBuilderAttribute("count")
    private int count;

    @PluginBuilderAttribute("bytes")
    private int bytes;

    @PluginBuilderAttribute("memory")
    private int memory;

    @PluginBuilderAttribute("retries")
    private int retries;

    @PluginBuilderAttribute("threads")
    private int threads;

    @PluginBuilderAttribute("formatter")
    private String formatter = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @PluginBuilderAttribute("timezone")
    private String timezone = "Asia/Shanghai";

    @PluginElement("fields")
    private KeyValuePair[] fields;

    @PluginElement("columns")
    private Columns columns;

    @PluginBuilderAttribute("debug")
    private Boolean debug = Boolean.FALSE;

    public String getProject() {
        return project;
    }

    public B setProject(String project) {
        this.project = project;
        return asBuilder();
    }

    public String getLogstore() {
        return logstore;
    }

    public B setLogstore(String logstore) {
        this.logstore = logstore;
        return asBuilder();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public B setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return asBuilder();
    }

    public String getAccessId() {
        return accessId;
    }

    public B setAccessId(String accessId) {
        this.accessId = accessId;
        return asBuilder();
    }

    public String getAccessKey() {
        return accessKey;
    }

    public B setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return asBuilder();
    }

    public String getStsToken() {
        return stsToken;
    }

    public B setStsToken(String stsToken) {
        this.stsToken = stsToken;
        return asBuilder();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public B setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return asBuilder();
    }

    public String getTopic() {
        return topic;
    }

    public B setTopic(String topic) {
        this.topic = topic;
        return asBuilder();
    }

    public String getModule() {
        return module;
    }

    public B setModule(String module) {
        this.module = module;
        return asBuilder();
    }

    public String getSource() {
        return source;
    }

    public B setSource(String source) {
        this.source = source;
        return asBuilder();
    }

    public String getHost() {
        return host;
    }

    public B setHost(String host) {
        this.host = host;
        return asBuilder();
    }

    public int getTimeout() {
        return timeout;
    }

    public B setTimeout(int timeout) {
        this.timeout = timeout;
        return asBuilder();
    }

    public int getCount() {
        return count;
    }

    public B setCount(int count) {
        this.count = count;
        return asBuilder();
    }

    public int getBytes() {
        return bytes;
    }

    public B setBytes(int bytes) {
        this.bytes = bytes;
        return asBuilder();
    }

    public int getMemory() {
        return memory;
    }

    public B setMemory(int memory) {
        this.memory = memory;
        return asBuilder();
    }

    public int getRetries() {
        return retries;
    }

    public B setRetries(int retries) {
        this.retries = retries;
        return asBuilder();
    }

    public int getThreads() {
        return threads;
    }

    public B setThreads(int threads) {
        this.threads = threads;
        return asBuilder();
    }

    public String getFormatter() {
        return formatter;
    }

    public B setFormatter(String formatter) {
        this.formatter = formatter;
        return asBuilder();
    }

    public String getTimezone() {
        return timezone;
    }

    public B setTimezone(String timezone) {
        this.timezone = timezone;
        return asBuilder();
    }

    public KeyValuePair[] getFields() {
        return fields;
    }

    public LoghubAppenderBuilder<B> setFields(KeyValuePair[] fields) {
        this.fields = fields;
        return asBuilder();
    }

    public Columns getColumns() {
        return columns;
    }

    public LoghubAppenderBuilder<B> setColumns(Columns columns) {
        this.columns = columns;
        return asBuilder();
    }

    public Boolean getDebug() {
        return debug;
    }

    public B setDebug(Boolean debug) {
        this.debug = debug;
        return asBuilder();
    }

    private ProjectConfig buildProjectConfig() {
        return new ProjectConfig(project, endpoint, accessId, accessKey, stsToken, userAgent);
    }

    private ProducerConfig buildProducerConfig() {
        ProducerConfig config = new ProducerConfig();
        if (timeout > 0) {
            config.setMaxBlockMs(timeout);
        }
        if (count > 0) {
            config.setBatchCountThreshold(count);
        }
        if (bytes > 0) {
            config.setBatchSizeThresholdInBytes(bytes);
        }
        if (memory > 0) {
            config.setTotalSizeInBytes(memory);
        }
        if (retries > 0) {
            config.setRetries(retries);
        }
        if (threads > 0) {
            config.setIoThreadCount(threads);
        }
        return config;
    }

    @Override
    public LoghubAppender build() {
        ProjectConfig projectConfig = buildProjectConfig();

        ProducerConfig producerConfig = buildProducerConfig();
        LinkedHashMap<String, String> predefined = new LinkedHashMap<>();
        for (KeyValuePair pair : getFields()) {
            predefined.put(pair.getKey(), pair.getValue());
        }
        String host = getHost();
        if (Strings.isBlank(host)) {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ignored) { }
        }
        Columns columns = getColumns();
        Map<String, String> includes = Objects.isNull(columns) ? Collections.emptyMap() : columns.getIncludes();
        Set<String> excludes = Objects.isNull(columns) ? Collections.emptySet() : columns.getExcludes();
        return new LoghubAppender(
            getName(),
            getFilter(),
            getLayout(),
            isIgnoreExceptions(),
            getLogstore(),
            getTopic(),
            getModule(),
            getSource(),
            host,
            projectConfig,
            producerConfig,
            DateTimeFormatter.ofPattern(getFormatter()),
            ZoneId.of(timezone),
            predefined,
            includes,
            excludes,
            getDebug()
        );
    }
}
