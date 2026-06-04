package com.example.fieldmapper.model;

public class DatasourceConfig {
    private String type;
    private String host;
    private int port;
    private String database;
    private String schema;
    private String username;
    private String password;
    private boolean sysdba;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isSysdba() { return sysdba; }
    public void setSysdba(boolean sysdba) { this.sysdba = sysdba; }
}
