package com.example.fieldmapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FieldMapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(FieldMapperApplication.class, args);
    }
}
