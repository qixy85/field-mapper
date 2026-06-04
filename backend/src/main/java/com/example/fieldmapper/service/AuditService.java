package com.example.fieldmapper.service;

import com.example.fieldmapper.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final JdbcTemplate meloneJdbcTemplate;

    public AuditService(JdbcTemplate meloneJdbcTemplate) {
        this.meloneJdbcTemplate = meloneJdbcTemplate;
    }

    private final RowMapper<AuditLog> rowMapper = (rs, rowNum) -> {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("ID"));
        log.setTableName(rs.getString("TABLE_NAME"));
        log.setRecordId(rs.getLong("RECORD_ID"));
        log.setActionType(rs.getString("ACTION_TYPE"));
        log.setFieldName(rs.getString("FIELD_NAME"));
        log.setOldValue(rs.getString("OLD_VALUE"));
        log.setNewValue(rs.getString("NEW_VALUE"));
        log.setModifiedBy(rs.getString("MODIFIED_BY"));
        if (rs.getTimestamp("MODIFIED_AT") != null)
            log.setModifiedAt(rs.getTimestamp("MODIFIED_AT").toLocalDateTime());
        return log;
    };

    public void log(String tableName, Long recordId, String actionType,
                    String fieldName, String oldValue, String newValue, String modifiedBy) {
        meloneJdbcTemplate.update(
                "INSERT INTO AUDIT_LOGS (TABLE_NAME, RECORD_ID, ACTION_TYPE, FIELD_NAME, " +
                "OLD_VALUE, NEW_VALUE, MODIFIED_BY, MODIFIED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)",
                tableName, recordId, actionType, fieldName, oldValue, newValue, modifiedBy);
    }

    public List<AuditLog> findAll() {
        return meloneJdbcTemplate.query("SELECT * FROM AUDIT_LOGS ORDER BY ID DESC", rowMapper);
    }
}
