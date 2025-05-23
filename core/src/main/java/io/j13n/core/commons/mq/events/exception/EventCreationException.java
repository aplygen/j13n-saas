package io.j13n.core.commons.mq.events.exception;

import java.io.Serial;

public class EventCreationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6415843079341795248L;

    public EventCreationException(String errorField) {
        super(errorField + " cannot be null or blank");
    }
}
