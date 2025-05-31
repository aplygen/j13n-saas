package io.j13n.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.j13n.core.commons.jooq.configuration.AbstractJooqBaseConfiguration;
import io.j13n.core.commons.jooq.jackson.UnsignedNumbersSerializationModule;
import io.j13n.core.commons.security.ISecurityConfiguration;
import io.j13n.core.service.CoreMessageResourceService;
import io.j13n.core.service.auth.AuthenticationService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class CoreConfiguration extends AbstractJooqBaseConfiguration implements ISecurityConfiguration {

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationService authService) throws Exception {
        return this.springSecurityFilterChain(http, authService, this.objectMapper);
    }
}
