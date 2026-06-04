package com.example.fieldmapper;

import com.example.fieldmapper.config.DatasourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DatasourceProperties.class)
public class FieldMapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(FieldMapperApplication.class, args);
    }
}
