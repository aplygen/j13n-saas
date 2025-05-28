package io.j13n.core.config;

import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

/**
 * Configuration class for jOOQ.
 * This class configures jOOQ to work with Spring and PostgreSQL.
 */
@Configuration
public class JooqConfig {

    /**
     * Creates a DSLContext bean that can be used to create jOOQ queries.
     * The DSLContext is configured to use PostgreSQL dialect and the provided DataSource.
     *
     * @param dataSource the DataSource to use for database connections
     * @return a configured DSLContext
     */
    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        // Wrap the DataSource in a TransactionAwareDataSourceProxy to ensure
        // that jOOQ uses the same transaction as Spring
        TransactionAwareDataSourceProxy transactionAwareDataSource = new TransactionAwareDataSourceProxy(dataSource);

        // Create a jOOQ configuration with the PostgreSQL dialect
        org.jooq.Configuration jooqConfig =
                new DefaultConfiguration().set(transactionAwareDataSource).set(SQLDialect.POSTGRES);

        // Create and return the DSLContext
        return DSL.using(jooqConfig);
    }
}
