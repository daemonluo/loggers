package com.daemon.loggers;

import java.io.Serializable;
import java.util.List;

public class Events implements Serializable {
    public static final String TAG_PARSE_ARGS_FAILED = "_parse_args_failed";
    public static final String TAG_PARSE_ARGS_PARTITAL_FAILED = "_parse_args_partial_failed";
    public static final String TAG_PARSE_MSG_FAILED = "_parse_msg_failed";

    private List<Event> events;
    private List<String> tags;

    public Events() {
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isEmpty() {
        return events == null || events.isEmpty();
    }
}
