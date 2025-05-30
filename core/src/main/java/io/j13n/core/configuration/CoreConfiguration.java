package io.j13n.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.j13n.core.commons.jooq.configuration.AbstractJooqBaseConfiguration;
import io.j13n.core.commons.jooq.jackson.UnsignedNumbersSerializationModule;
import io.j13n.core.service.CoreMessageResourceService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfiguration extends AbstractJooqBaseConfiguration {

    protected CoreMessageResourceService coreMessageResourceService;

    protected CoreConfiguration(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @PostConstruct
    @Override
    public void initialize() {
        super.initialize(coreMessageResourceService);
        this.objectMapper.registerModule(new UnsignedNumbersSerializationModule(coreMessageResourceService));
    }
}
