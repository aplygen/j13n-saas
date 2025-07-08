package io.j13n.core.commons.mq.events;

import io.j13n.commons.util.StringFormatter;

public class EventNames {

    public static final String USER_CREATED = "USER_CREATED";

    private EventNames() {}

    public static String getEventName(String eventName, Object... args) {
        return StringFormatter.format(eventName, args);
    }
}
