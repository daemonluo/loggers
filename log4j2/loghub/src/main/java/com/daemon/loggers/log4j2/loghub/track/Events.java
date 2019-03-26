package com.daemon.loggers.log4j2.loghub.track;

import java.io.Serializable;
import java.util.List;

public class Events implements Serializable {
    static final String TAG_PARSE_ARGS_FAILED = "_parse_args_failed";
    static final String TAG_PARSE_ARGS_PARTITAL_FAILED = "_parse_args_partial_failed";
    static final String TAG_PARSE_MSG_FAILED = "_parse_msg_failed";

    private List<Event> events;
    private List<String> tags;

    Events() {
    }

    public List<Event> getEvents() {
        return events;
    }

    void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<String> getTags() {
        return tags;
    }

    void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isEmpty() {
        return events == null || events.isEmpty();
    }
}
