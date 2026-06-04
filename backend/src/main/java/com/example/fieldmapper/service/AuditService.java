package com.example.fieldmapper.service;

import com.example.fieldmapper.model.AuditLog;
import com.example.fieldmapper.model.BudgetItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        log.setOldValue(rs.getString("OLD_VALUE"));
        log.setNewValue(rs.getString("NEW_VALUE"));
        log.setModifiedBy(rs.getString("MODIFIED_BY"));
        if (rs.getTimestamp("MODIFIED_AT") != null)
            log.setModifiedAt(rs.getTimestamp("MODIFIED_AT").toLocalDateTime());
        return log;
    };

    public void logRow(String action, Long recordId, BudgetItem oldRow, BudgetItem newRow, String username) {
        String oldJson = oldRow != null ? toJson(oldRow) : null;
        String newJson = newRow != null ? toJson(newRow) : null;
        meloneJdbcTemplate.update(
                "INSERT INTO AUDIT_LOGS (TABLE_NAME, RECORD_ID, ACTION_TYPE, FIELD_NAME, " +
                "OLD_VALUE, NEW_VALUE, MODIFIED_BY, MODIFIED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)",
                "BUDGET_ITEMS", recordId, action, "ROW_DATA", oldJson, newJson, username);
    }

    public List<AuditLog> findAll() {
        return meloneJdbcTemplate.query(
                "SELECT * FROM AUDIT_LOGS WHERE FIELD_NAME = 'ROW_DATA' ORDER BY ID DESC", rowMapper);
    }

    private String toJson(BudgetItem item) {
        Map<String, String> m = new LinkedHashMap<>();
        put(m, "去向", item.getDirection());
        put(m, "项目", item.getProject());
        put(m, "片区名", item.getArea());
        put(m, "具体费用", item.getSpecificCost());
        put(m, "月份", item.getMonth());
        put(m, "实际累计金额", item.getActualCumulative() != null ? item.getActualCumulative().toPlainString() : "");
        put(m, "当月账面累计金额", item.getBookCumulative() != null ? item.getBookCumulative().toPlainString() : "");
        put(m, "当月差异", item.getDifference() != null ? item.getDifference().toPlainString() : "");
        put(m, "备注/服务期限", item.getRemark());
        put(m, "数据核对人", item.getVerifier());
        return m.toString();
    }

    private void put(Map<String, String> m, String key, String val) {
        if (val != null && !val.isEmpty()) m.put(key, val);
    }
}
