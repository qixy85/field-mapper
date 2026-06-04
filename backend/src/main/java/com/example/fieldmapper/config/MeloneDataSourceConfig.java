package com.example.fieldmapper.config;

import com.example.fieldmapper.model.DatasourceConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MeloneDataSourceConfig {

    private final DatasourceProperties datasourceProperties;
    private final String dsKey;

    public MeloneDataSourceConfig(DatasourceProperties datasourceProperties,
                                  @Value("${app.business-datasource:melone}") String dsKey) {
        this.datasourceProperties = datasourceProperties;
        this.dsKey = dsKey;
    }

    @Bean
    public DataSource meloneDataSource() {
        DatasourceConfig cfg = datasourceProperties.getDatasources().get(dsKey);
        if (cfg == null) {
            throw new IllegalStateException(
                "Datasource '" + dsKey + "' not found. Available: " + datasourceProperties.getDatasources().keySet());
        }
        HikariConfig hikari = new HikariConfig();
        if ("postgresql".equalsIgnoreCase(cfg.getType())) {
            hikari.setJdbcUrl("jdbc:postgresql://" + cfg.getHost() + ":" + cfg.getPort() + "/" + cfg.getDatabase());
            hikari.setDriverClassName("org.postgresql.Driver");
            hikari.setConnectionTestQuery("SELECT 1");
        } else {
            hikari.setJdbcUrl("jdbc:oracle:thin:@//" + cfg.getHost() + ":" + cfg.getPort() + "/" + cfg.getDatabase());
            hikari.setDriverClassName("oracle.jdbc.OracleDriver");
            hikari.setConnectionTestQuery("SELECT 1 FROM DUAL");
        }
        hikari.setUsername(cfg.getUsername());
        hikari.setPassword(cfg.getPassword());
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);
        return new HikariDataSource(hikari);
    }

    @Bean
    public JdbcTemplate meloneJdbcTemplate(DataSource meloneDataSource) {
        return new JdbcTemplate(meloneDataSource);
    }
}
