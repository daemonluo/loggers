package com.daemon.loggers.log4j2.loghub;

import com.aliyun.openservices.log.producer.ProducerConfig;
import com.aliyun.openservices.log.producer.ProjectConfig;
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

    @PluginBuilderAttribute("topic")
    private String topic;

    @PluginBuilderAttribute("module")
    private String module;

    @PluginBuilderAttribute("source")
    private String source;

    @PluginBuilderAttribute("host")
    private String host;

    @PluginBuilderAttribute("timeout")
    private int packageTimeoutInMS;

    @PluginBuilderAttribute("size")
    private int logsCountPerPackage;

    @PluginBuilderAttribute("count")
    private int logsBytesPerPackage;

    @PluginBuilderAttribute("memory")
    private int memPoolSizeInByte;

    @PluginBuilderAttribute("retry")
    private int retryTimes;

    @PluginBuilderAttribute("threads")
    private int maxIOThreadSizeInPool;

    @PluginBuilderAttribute("formatter")
    private String formatter = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @PluginBuilderAttribute("timezone")
    private String timezone = "Asia/Shanghai";

    @PluginElement("fields")
    private KeyValuePair[] fields;

    @PluginElement("columns")
    private Columns columns;

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

    public int getPackageTimeoutInMS() {
        return packageTimeoutInMS;
    }

    public B setPackageTimeoutInMS(int packageTimeoutInMS) {
        this.packageTimeoutInMS = packageTimeoutInMS;
        return asBuilder();
    }

    public int getLogsCountPerPackage() {
        return logsCountPerPackage;
    }

    public B setLogsCountPerPackage(int logsCountPerPackage) {
        this.logsCountPerPackage = logsCountPerPackage;
        return asBuilder();
    }

    public int getLogsBytesPerPackage() {
        return logsBytesPerPackage;
    }

    public B setLogsBytesPerPackage(int logsBytesPerPackage) {
        this.logsBytesPerPackage = logsBytesPerPackage;
        return asBuilder();
    }

    public int getMemPoolSizeInByte() {
        return memPoolSizeInByte;
    }

    public B setMemPoolSizeInByte(int memPoolSizeInByte) {
        this.memPoolSizeInByte = memPoolSizeInByte;
        return asBuilder();
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public B setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return asBuilder();
    }

    public int getMaxIOThreadSizeInPool() {
        return maxIOThreadSizeInPool;
    }

    public B setMaxIOThreadSizeInPool(int maxIOThreadSizeInPool) {
        this.maxIOThreadSizeInPool = maxIOThreadSizeInPool;
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

    @Override
    public LoghubAppender build() {
        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.projectName = getProject();
        projectConfig.accessKeyId = getAccessId();
        projectConfig.accessKey = getAccessKey();
        projectConfig.endpoint = getEndpoint();
        if (getStsToken() != null) {
            projectConfig.stsToken = getStsToken();
        }

        ProducerConfig producerConfig = new ProducerConfig();
        if (getPackageTimeoutInMS() > 0) {
            producerConfig.packageTimeoutInMS = getPackageTimeoutInMS();
        }
        if (getLogsCountPerPackage() > 0) {
            producerConfig.logsCountPerPackage = getLogsCountPerPackage();
        }
        if (getLogsBytesPerPackage() > 0) {
            producerConfig.logsBytesPerPackage = getLogsBytesPerPackage();
        }
        if (getMemPoolSizeInByte() > 0) {
            producerConfig.memPoolSizeInByte = getMemPoolSizeInByte();
        }
        if (getRetryTimes() > 0) {
            producerConfig.retryTimes = getRetryTimes();
        }
        if (getMaxIOThreadSizeInPool() > 0) {
            producerConfig.maxIOThreadSizeInPool = getMaxIOThreadSizeInPool();
        }
        producerConfig.userAgent = "log4j2";
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
            excludes
        );
    }
}
