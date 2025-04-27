package io.j13n.search.config;

import io.j13n.commons.service.VirtualThreadManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadConfig {

    @Bean
    public VirtualThreadManager virtualThreadManager() {
        return new VirtualThreadManager();
    }
}
