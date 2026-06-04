package com.example.fieldmapper.service;

import com.example.fieldmapper.config.DatasourceProperties;
import com.example.fieldmapper.model.DatasourceConfig;
import com.example.fieldmapper.model.MappingRequest;
import com.example.fieldmapper.model.MappingRequest.FieldMapping;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final DatasourceProperties datasourceProperties;

    public ExportService(DatasourceProperties datasourceProperties) {
        this.datasourceProperties = datasourceProperties;
    }

    public File export(MappingRequest req) throws Exception {
        DatasourceConfig ds = resolveDatasource(req.getDatasource());
        validate(req, ds);
        String url = buildJdbcUrl(ds);

        List<FieldMapping> validMappings = req.getMappings().stream()
                .filter(m -> m.getDbField() != null && !m.getDbField().isBlank()
                        && m.getHeader() != null && !m.getHeader().isBlank())
                .collect(Collectors.toList());

        String columns = validMappings.stream()
                .map(m -> quoteIdentifier(m.getDbField(), ds.getType()))
                .collect(Collectors.joining(", "));
        String tableRef = buildTableRef(req.getTableName(), ds);
        String sql = "SELECT " + columns + " FROM " + tableRef;
        log.info("Executing SQL: {}", sql);

        Properties connProps = new Properties();
        connProps.setProperty("user", ds.getUsername());
        connProps.setProperty("password", ds.getPassword());
        if (ds.isSysdba() && "oracle".equalsIgnoreCase(ds.getType())) {
            connProps.setProperty("internal_logon", "sysdba");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, connProps);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = meta.getColumnLabel(i);
                    Object val = rs.getObject(i);
                    row.put(colName, val != null ? val : "");
                }
                rows.add(row);
            }
        }

        File outputDir = new File(System.getProperty("java.io.tmpdir"), "field-mapper-exports");
        outputDir.mkdirs();
        File file = new File(outputDir, "export_" + System.currentTimeMillis() + ".xlsx");

        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = wb.createSheet("Data");

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = wb.createFont();
            headerFont.setFontName("Microsoft YaHei");
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = wb.createCellStyle();
            Font dataFont = wb.createFont();
            dataFont.setFontName("Microsoft YaHei");
            dataFont.setFontHeightInPoints((short) 12);
            dataStyle.setFont(dataFont);
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            List<String> headers = validMappings.stream()
                    .map(FieldMapping::getHeader).collect(Collectors.toList());
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            for (int r = 0; r < rows.size(); r++) {
                Row dataRow = sheet.createRow(r + 1);
                Map<String, Object> rowData = rows.get(r);
                int c = 0;
                for (FieldMapping fm : validMappings) {
                    Object val = rowData.get(fm.getDbField());
                    Cell cell = dataRow.createCell(c);
                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                    } else if (val instanceof java.util.Date) {
                        cell.setCellValue((java.util.Date) val);
                    } else if (val instanceof Timestamp) {
                        cell.setCellValue((Timestamp) val);
                    } else {

                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                    cell.setCellStyle(dataStyle);
                    c++;
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
        }

        log.info("Excel exported to: {}", file.getAbsolutePath());
        return file;
    }

    private DatasourceConfig resolveDatasource(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("datasource key is required");
        }
        Map<String, DatasourceConfig> all = datasourceProperties.getDatasources();
        DatasourceConfig ds = all.get(key);
        if (ds == null) {
            throw new IllegalArgumentException("Unknown datasource: " + key
                    + ". Available: " + String.join(", ", all.keySet()));
        }
        return ds;
    }

    private String buildJdbcUrl(DatasourceConfig ds) {
        if ("oracle".equalsIgnoreCase(ds.getType())) {
            return "jdbc:oracle:thin:@" + ds.getHost() + ":" + ds.getPort() + ":" + ds.getDatabase();
        }
        return "jdbc:postgresql://" + ds.getHost() + ":" + ds.getPort() + "/" + ds.getDatabase();
    }

    private String buildTableRef(String tableName, DatasourceConfig ds) {
        String schema = (ds.getSchema() != null && !ds.getSchema().isBlank())
                ? ds.getSchema() : ("oracle".equalsIgnoreCase(ds.getType()) ? ds.getUsername().toUpperCase() : "public");
        return quoteIdentifier(schema, ds.getType()) + "." + quoteIdentifier(tableName, ds.getType());
    }

    private String quoteIdentifier(String name, String dbType) {
        String s = name;
        if ("oracle".equalsIgnoreCase(dbType)) {
            s = s.toUpperCase();
        }
        return "\"" + s + "\"";
    }

    private void validate(MappingRequest req, DatasourceConfig ds) {
        if (req.getTableName() == null || req.getTableName().isBlank()) {
            throw new IllegalArgumentException("tableName is required");
        }
        if (req.getMappings() == null || req.getMappings().isEmpty()) {
            throw new IllegalArgumentException("at least one field mapping is required");
        }
    }
}
