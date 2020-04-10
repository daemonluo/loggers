package com.daemon.loggers.logback;

import com.daemon.loggers.Event;
import com.daemon.loggers.SimpleEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LoghubAppenderTest {
    @Test
    public void testLoggerWithLayout() {
        final Logger logger = LoggerFactory.getLogger("loghub-appender-without-layout");
        logger.info("loghub-appender-without-layout");
    }

    @Test
    public void testLoggerWithPatternLayout() {
        final Logger logger = LoggerFactory.getLogger("loghub-appender-with-pattern-layout");
        logger.info("loghub-appender-with-pattern-layout");
        logger.info("loghub-appender-with-pattern-layout with args: {}, {}", 1, 2);
    }

    @Test
    public void testLoggerWithTrackLayout() {
        final Logger logger = LoggerFactory.getLogger("loghub-appender-with-track-layout");
        logger.info("loghub-appender-with-track-layout");
        logger.info("loghub-appender-with-track-layout: {}, {}", 1, 2);
    }

    @Test
    public void testLoggerWithTrackEvent() {
        final Logger logger = LoggerFactory.getLogger("loghub-appender-with-track-event");
        logger.info("loghub-appender-with-track-event");
        Map<String, String> map = new HashMap<>();
        map.put("event", "event_test");
        map.put("test", "test");
        map.put("d1", "v1");
        map.put("client", "   ");
        Event event = new SimpleEvent("event_test")
            .client("")
            .platform("   ")
            .property("k1", "v1")
            .property("k2", "v2")
            .time(System.currentTimeMillis());
        logger.info("{}, {}", map, event);
    }
}
