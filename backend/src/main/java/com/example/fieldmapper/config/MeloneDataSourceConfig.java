package com.example.fieldmapper.config;

import com.example.fieldmapper.model.DatasourceConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MeloneDataSourceConfig {

    private final DatasourceProperties datasourceProperties;

    public MeloneDataSourceConfig(DatasourceProperties datasourceProperties) {
        this.datasourceProperties = datasourceProperties;
    }

    @Bean
    public DataSource meloneDataSource() {
        DatasourceConfig cfg = datasourceProperties.getDatasources().get("melone");
        if (cfg == null) {
            throw new IllegalStateException("melone datasource not configured in app.datasources");
        }
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl("jdbc:oracle:thin:@//" + cfg.getHost() + ":" + cfg.getPort() + "/" + cfg.getDatabase());
        hikari.setUsername(cfg.getUsername());
        hikari.setPassword(cfg.getPassword());
        hikari.setDriverClassName("oracle.jdbc.OracleDriver");
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);
        hikari.setConnectionTestQuery("SELECT 1 FROM DUAL");
        return new HikariDataSource(hikari);
    }

    @Bean
    public JdbcTemplate meloneJdbcTemplate(DataSource meloneDataSource) {
        return new JdbcTemplate(meloneDataSource);
    }
}
