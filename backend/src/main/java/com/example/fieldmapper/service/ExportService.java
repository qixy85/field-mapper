package com.example.fieldmapper.service;

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

    public File export(MappingRequest req) throws Exception {
        validate(req);
        String url = buildJdbcUrl(req);
        List<FieldMapping> validMappings = req.getMappings().stream()
                .filter(m -> m.getDbField() != null && !m.getDbField().isBlank()
                        && m.getHeader() != null && !m.getHeader().isBlank())
                .collect(Collectors.toList());

        if (validMappings.isEmpty()) {
            throw new IllegalArgumentException("No valid field mappings provided");
        }

        // Build SELECT SQL with quoted identifiers
        String columns = validMappings.stream()
                .map(m -> quoteIdentifier(m.getDbField(), req.getDbType()))
                .collect(Collectors.joining(", "));
        String tableRef = buildTableRef(req);
        String sql = "SELECT " + columns + " FROM " + tableRef;
        log.info("Executing SQL: {}", sql);

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, req.getUsername(), req.getPassword());
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

        // Generate Excel
        File outputDir = new File(System.getProperty("java.io.tmpdir"), "field-mapper-exports");
        outputDir.mkdirs();
        File file = new File(outputDir, "export_" + System.currentTimeMillis() + ".xlsx");

        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = wb.createSheet("Data");

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(createFont(wb, true, (short) 14));
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setFont(createFont(wb, false, (short) 12));
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Header row
            Row headerRow = sheet.createRow(0);
            List<String> headers = validMappings.stream()
                    .map(FieldMapping::getHeader).collect(Collectors.toList());
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
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
                        CellStyle dateStyle = wb.createCellStyle();
                        dateStyle.setDataFormat(wb.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
                        dateStyle.cloneStyleFrom(dataStyle);
                        cell.setCellStyle(dateStyle);
                        c++;
                        continue;
                    } else {
                        cell.setCellValue(val != null ? val.toString() : "");
                    }
                    cell.setCellStyle(dataStyle);
                    c++;
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
        }

        log.info("Excel exported to: {}", file.getAbsolutePath());
        return file;
    }

    private String buildJdbcUrl(MappingRequest req) {
        String url;
        if ("oracle".equalsIgnoreCase(req.getDbType())) {
            url = "jdbc:oracle:thin:@" + req.getHost() + ":" + req.getPort() + ":" + req.getDatabase();
        } else {
            url = "jdbc:postgresql://" + req.getHost() + ":" + req.getPort() + "/" + req.getDatabase();
        }
        log.info("JDBC URL: {}", url);
        return url;
    }

    private String buildTableRef(MappingRequest req) {
        if ("oracle".equalsIgnoreCase(req.getDbType())) {
            String schema = (req.getSchema() != null && !req.getSchema().isBlank())
                    ? req.getSchema() : req.getUsername().toUpperCase();
            return quoteIdentifier(schema, req.getDbType()) + "." + quoteIdentifier(req.getTableName(), req.getDbType());
        } else {
            String schema = (req.getSchema() != null && !req.getSchema().isBlank())
                    ? req.getSchema() : "public";
            return quoteIdentifier(schema, req.getDbType()) + "." + quoteIdentifier(req.getTableName(), req.getDbType());
        }
    }

    private String quoteIdentifier(String name, String dbType) {
        if ("oracle".equalsIgnoreCase(dbType)) {
            return "\"" + name.toUpperCase() + "\"";
        }
        return "\"" + name + "\"";
    }

    private Font createFont(Workbook wb, boolean bold, short size) {
        Font font = wb.createFont();
        font.setFontName("Microsoft YaHei");
        font.setBold(bold);
        font.setFontHeightInPoints(size);
        return font;
    }

    private void validate(MappingRequest req) {
        if (req.getDbType() == null || req.getDbType().isBlank()) {
            throw new IllegalArgumentException("dbType is required (postgresql / oracle)");
        }
        if (req.getHost() == null || req.getHost().isBlank()) {
            throw new IllegalArgumentException("host is required");
        }
        if (req.getPort() <= 0) {
            throw new IllegalArgumentException("port is required");
        }
        if (req.getDatabase() == null || req.getDatabase().isBlank()) {
            throw new IllegalArgumentException("database is required");
        }
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (req.getTableName() == null || req.getTableName().isBlank()) {
            throw new IllegalArgumentException("tableName is required");
        }
        if (req.getMappings() == null || req.getMappings().isEmpty()) {
            throw new IllegalArgumentException("at least one field mapping is required");
        }
    }
}
