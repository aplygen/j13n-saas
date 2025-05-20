package com.fincity.saas.commons.configuration;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincity.saas.commons.service.AbstractMessageService;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

@Getter
public abstract class AbstractJdbcBaseConfiguration extends AbstractBaseConfiguration {

    @Value("${spring.datasource.url}")
    protected String url;

    @Value("${spring.datasource.username}")
    protected String username;

    @Value("${spring.datasource.password}")
    protected String password;

    protected AbstractJdbcBaseConfiguration(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @PostConstruct
    public void initialize(AbstractMessageService messageResourceService) {
        super.initialize();
        this.objectMapper.registerModule(
                new com.fincity.saas.commons.jooq.jackson.UnsignedNumbersSerializationModule(messageResourceService));
    }

    @Bean
    public DSLContext context() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // Wrap the datasource in a transaction-aware proxy
        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(dataSource);

        // Create and return the DSLContext
        return DSL.using(proxy, SQLDialect.POSTGRES);
    }
}