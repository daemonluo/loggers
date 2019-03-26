package com.daemon.loggers.log4j2.loghub;

import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.producer.ILogCallback;
import com.aliyun.openservices.log.response.PutLogsResponse;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class LoghubAppenderCallback extends ILogCallback {
    private Logger logger;

    private String project;

    private String logstore;

    private String topic;

    private String source;

    private List<LogItem> logItems;

    LoghubAppenderCallback(Logger logger, String project, String logstore, String topic, String source, List<LogItem> logItems) {
        super();
        this.logger = logger;
        this.project = project;
        this.logstore = logstore;
        this.topic = topic;
        this.source = source;
        this.logItems = logItems;
    }

    public void onCompletion(PutLogsResponse putLogsResponse, LogException e) {
        if (e != null) {
            logger.error("Failed to putLogs. project=" + project + " logstore=" + logstore + " topic=" + topic + " source=" + source + " logItems=" + logItems, e);
        }
    }
}
