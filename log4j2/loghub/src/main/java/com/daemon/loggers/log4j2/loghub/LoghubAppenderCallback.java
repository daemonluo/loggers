package com.daemon.loggers.log4j2.loghub;

import com.aliyun.openservices.aliyun.log.producer.Callback;
import com.aliyun.openservices.aliyun.log.producer.Result;
import com.aliyun.openservices.log.common.LogItem;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LoghubAppenderCallback implements Callback {
    protected Logger logger;

    protected String project;

    protected String logstore;

    protected String topic;

    protected String source;

    protected List<LogItem> logItems;

    public LoghubAppenderCallback(Logger logger, String project, String logstore, String topic, String source, List<LogItem> logItems) {
        super();
        this.logger = logger;
        this.project = project;
        this.logstore = logstore;
        this.topic = topic;
        this.source = source;
        this.logItems = logItems;
    }

    @Override
    public void onCompletion(Result result) {
        if (!result.isSuccessful()) {
            logger.error(
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
