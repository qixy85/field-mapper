package com.example.fieldmapper.service;

import com.example.fieldmapper.model.AuditLog;
import com.example.fieldmapper.model.BudgetItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
        if ("INSERT".equals(action)) {
            String json = toJson(newRow, true);
            meloneJdbcTemplate.update(
                    "INSERT INTO AUDIT_LOGS (TABLE_NAME, RECORD_ID, ACTION_TYPE, FIELD_NAME, " +
                    "OLD_VALUE, NEW_VALUE, MODIFIED_BY, MODIFIED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)",
                    "BUDGET_ITEMS", recordId, "INSERT", "ROW_DATA", null, json, username);
        } else {
            // Build full old/new maps
            Map<String, String> oldMap = new LinkedHashMap<>();
            Map<String, String> newMap = new LinkedHashMap<>();
            allFields(oldMap, oldRow);
            allFields(newMap, newRow);

            // If identical, skip
            if (oldMap.toString().equals(newMap.toString())) return;

            // Only keep changed fields
            Map<String, String> oldDiff = new LinkedHashMap<>();
            Map<String, String> newDiff = new LinkedHashMap<>();
            for (String k : oldMap.keySet()) {
                String o = oldMap.get(k);
                String n = newMap.getOrDefault(k, "");
                if (!o.equals(n)) {
                    oldDiff.put(k, o.isEmpty() ? "(空)" : o);
                    newDiff.put(k, n.isEmpty() ? "(空)" : n);
                }
            }
            for (String k : newMap.keySet()) {
                if (!oldMap.containsKey(k)) {
                    String n = newMap.get(k);
                    if (!n.isEmpty()) {
                        oldDiff.put(k, "(空)");
                        newDiff.put(k, n);
                    }
                }
            }

            if (oldDiff.isEmpty() && newDiff.isEmpty()) return;

            meloneJdbcTemplate.update(
                    "INSERT INTO AUDIT_LOGS (TABLE_NAME, RECORD_ID, ACTION_TYPE, FIELD_NAME, " +
                    "OLD_VALUE, NEW_VALUE, MODIFIED_BY, MODIFIED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)",
                    "BUDGET_ITEMS", recordId, "UPDATE", "ROW_DATA",
                    oldDiff.toString(), newDiff.toString(), username);
        }
    }

    public List<AuditLog> findAll() {
        return meloneJdbcTemplate.query(
                "SELECT * FROM AUDIT_LOGS WHERE FIELD_NAME = 'ROW_DATA' ORDER BY ID DESC", rowMapper);
    }

    private void allFields(Map<String, String> m, BudgetItem item) {
        if (item == null) return;
        val(m, "去向", item.getDirection());
        val(m, "项目", item.getProject());
        val(m, "片区名", item.getArea());
        val(m, "具体费用", item.getSpecificCost());
        val(m, "月份", item.getMonth());
        num(m, "实际累计金额", item.getActualCumulative());
        num(m, "当月账面累计金额", item.getBookCumulative());
        num(m, "当月差异", item.getDifference());
        val(m, "备注/服务期限", item.getRemark());
        val(m, "数据核对人", item.getVerifier());
    }

    private void val(Map<String, String> m, String key, String v) {
        m.put(key, v != null ? v.trim() : "");
    }

    private void num(Map<String, String> m, String key, BigDecimal v) {
        m.put(key, v != null ? v.stripTrailingZeros().toPlainString() : "");
    }

    private String toJson(BudgetItem item, boolean all) {
        Map<String, String> m = new LinkedHashMap<>();
        allFields(m, item);
        m.values().removeIf(v -> v.isEmpty());
        return m.toString();
    }
}
