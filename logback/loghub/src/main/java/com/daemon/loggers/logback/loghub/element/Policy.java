package com.daemon.loggers.logback.loghub.element;

import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Policy {
    private int timeout;

    private int count;

    private int bytes;

    private int memory;

    private int retries;

    private int threads;

    private String formatter = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private String timezone = "Asia/Shanghai";

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern(formatter);
    }

    public ZoneId getTimeZone() {
        return ZoneId.of(timezone);
    }

    public ProducerConfig buildProducerConfig() {
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
}
