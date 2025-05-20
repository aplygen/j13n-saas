package io.j13n.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.j13n.core.commons.jooq.configuration.AbstractJooqBaseConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfiguration extends AbstractJooqBaseConfiguration {

    protected CoreConfiguration(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
