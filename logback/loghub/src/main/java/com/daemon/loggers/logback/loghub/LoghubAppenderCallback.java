package com.daemon.loggers.logback.loghub;

import com.aliyun.openservices.aliyun.log.producer.Callback;
import com.aliyun.openservices.aliyun.log.producer.Result;
import com.aliyun.openservices.log.common.LogItem;

import java.util.List;

public class LoghubAppenderCallback implements Callback {
    protected LoghubAppender loghubAppender;

    protected String project;

    protected String logstore;

    protected String topic;

    protected String source;

    protected List<LogItem> logItems;

    public LoghubAppenderCallback(LoghubAppender loghubAppender, String project, String logstore, String topic, String source, List<LogItem> logItems) {
        super();
        this.loghubAppender = loghubAppender;
        this.project = project;
        this.logstore = logstore;
        this.topic = topic;
        this.source = source;
        this.logItems = logItems;
    }

    @Override
    public void onCompletion(Result result) {
        if (!result.isSuccessful()) {
            loghubAppender.addError(
                "Failed to send log, project=" + project
                    + ", logStore=" + logstore
                    + ", topic=" + topic
                    + ", source=" + source
                    + ", logItem=" + logItems
                    + ", errorCode=" + result.getErrorCode()
                    + ", errorMessage=" + result.getErrorMessage());
        }
    }
}

