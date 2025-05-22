package io.j13n.core.commons.mq.events;

import io.j13n.core.commons.security.jwt.ContextAuthentication;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class EventQueObject implements Serializable {

	@Serial
    private static final long serialVersionUID = 405434468251584343L;

    private String eventName;
    private String clientCode;
    private String appCode;
    private String xDebug;
    private Map<String, Object> data; // NOSONAR
    private ContextAuthentication authentication;
}
