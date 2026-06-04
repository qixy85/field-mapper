package com.example.fieldmapper.config;

import com.example.fieldmapper.model.DatasourceConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "datasources")
public class DatasourceProperties {

    private final Map<String, DatasourceConfig> datasources = new LinkedHashMap<>();

    public Map<String, DatasourceConfig> getDatasources() {
        return datasources;
    }
}
