package com.daemon.loggers.logback.loghub.element;

public class Destination {
    /**
     * 日志主题
     * 用于分类管理日志
     * 默认用于指代项目
     */
    private String topic;

    /**
     * 日志生成模块
     * 用于分类管理日志
     * 默认为Logger的名称
     */
    private String module;

    /**
     * 日志来源
     * 默认为主机IP
     */
    private String source;

    /**
     * 生成日志的主机
     * 默认为主机名
     */
    private String host;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
