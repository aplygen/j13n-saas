package io.j13n.core.commons.jooq.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.j13n.commons.configuration.AbstractBaseConfiguration;
import io.j13n.commons.configuration.service.AbstractMessageService;
import io.j13n.core.commons.jooq.jackson.UnsignedNumbersSerializationModule;
import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

@Getter
public abstract class AbstractJooqBaseConfiguration extends AbstractBaseConfiguration {

    @Value("${spring.datasource.url}")
    protected String url;

    @Value("${spring.datasource.username}")
    protected String username;

    @Value("${spring.datasource.password}")
    protected String password;

    protected AbstractJooqBaseConfiguration(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public void initialize(AbstractMessageService messageResourceService) {
        super.initialize();
        this.objectMapper.registerModule(new UnsignedNumbersSerializationModule(messageResourceService));
    }

    @Bean
    public DSLContext context() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(dataSource);

        return DSL.using(proxy, SQLDialect.POSTGRES);
    }
}
