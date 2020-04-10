package com.daemon.loggers.logback.loghub.element;

import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.Optional;

public class Auth {
    /**
     * loghub地址
     */
    private String endpoint;

    /**
     * Loghub配置
     */
    private String project;

    /**
     * Loghub配置
     */
    private String logstore;

    /**
     * access id
     */
    private String accessId;

    /**
     * access key
     */
    private String accessKey;

    /**
     * token
     */
    private String token;

    private String userAgent = "logback-loghub-appender";

    public void setEndpoint(String endpoint) {
        Optional.ofNullable(endpoint)
            .map(StringUtils::trimToNull)
            .ifPresent(v -> this.endpoint = v);
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        Optional.ofNullable(project)
            .map(StringUtils::trimToNull)
            .ifPresent(v -> this.project = v);
    }

    public String getLogstore() {
        return logstore;
    }

    public void setLogstore(String logstore) {
        Optional.ofNullable(logstore)
            .map(StringUtils::trimToNull)
            .ifPresent(v -> this.logstore = v);
    }

    public void setAccessId(String accessId) {
        Optional.ofNullable(accessId)
            .map(StringUtils::trimToNull)
            .ifPresent(v -> this.accessId = v);
    }

    public void setAccessKey(String accessKey) {
        Optional.ofNullable(accessKey)
            .map(StringUtils::trimToNull)
            .ifPresent(v -> this.accessKey = v);
    }

    public void setToken(String token) {
        Optional.ofNullable(token)
            .map(StringUtils::trimToNull)
            .ifPresent(v -> this.token = v);
    }

    public void setUserAgent(String userAgent) {
        Optional.ofNullable(userAgent)
            .map(StringUtils::trimToNull)
            .ifPresent(v -> this.userAgent = v);
    }

    public ProjectConfig buildProjectConfig() {
        return new ProjectConfig(project, endpoint, accessId, accessKey, token, userAgent);
    }
}
