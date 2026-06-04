package com.example.fieldmapper.model;

import java.util.List;

public class MappingRequest {

    private String dbType;       // "postgresql" or "oracle"
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String tableName;    // table or view to query
    private String schema;       // optional, default "public" for pg
    private List<FieldMapping> mappings;

    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }
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
