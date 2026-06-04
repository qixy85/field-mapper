package com.example.fieldmapper.model;

import java.util.List;

public class MappingRequest {

    private String datasource;  // key from application.yml datasources
    private String tableName;
    private List<FieldMapping> mappings;

    public String getDatasource() { return datasource; }
    public void setDatasource(String datasource) { this.datasource = datasource; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public List<FieldMapping> getMappings() { return mappings; }
    public void setMappings(List<FieldMapping> mappings) { this.mappings = mappings; }

    public static class FieldMapping {
        private String dbField;
        private String header;

        public String getDbField() { return dbField; }
        public void setDbField(String dbField) { this.dbField = dbField; }
        public String getHeader() { return header; }
        public void setHeader(String header) { this.header = header; }
    }
}
