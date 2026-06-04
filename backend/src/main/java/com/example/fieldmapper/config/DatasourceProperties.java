package com.example.fieldmapper.config;

import com.example.fieldmapper.model.DatasourceConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
public class DatasourceProperties {

    private final Map<String, DatasourceConfig> datasources = new LinkedHashMap<>();

    public Map<String, DatasourceConfig> getDatasources() {
        return datasources;
    }
}
