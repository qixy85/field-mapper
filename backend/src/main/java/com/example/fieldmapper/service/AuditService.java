package com.example.fieldmapper.service;

import com.example.fieldmapper.model.AuditLog;
import com.example.fieldmapper.model.BudgetItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        if ("INSERT".equals(action)) {
            String json = toJson(newRow);
            meloneJdbcTemplate.update(
                    "INSERT INTO AUDIT_LOGS (TABLE_NAME, RECORD_ID, ACTION_TYPE, FIELD_NAME, " +
                    "OLD_VALUE, NEW_VALUE, MODIFIED_BY, MODIFIED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)",
                    "BUDGET_ITEMS", recordId, "INSERT", "ROW_DATA", null, json, username);
        } else {
            // Only store changed fields
            Map<String, String> oldDiff = new LinkedHashMap<>();
            Map<String, String> newDiff = new LinkedHashMap<>();
            compare(oldDiff, newDiff, "去向", oldRow.getDirection(), newRow.getDirection());
            compare(oldDiff, newDiff, "项目", oldRow.getProject(), newRow.getProject());
            compare(oldDiff, newDiff, "片区名", oldRow.getArea(), newRow.getArea());
            compare(oldDiff, newDiff, "具体费用", oldRow.getSpecificCost(), newRow.getSpecificCost());
            compare(oldDiff, newDiff, "月份", oldRow.getMonth(), newRow.getMonth());
            compareNum(oldDiff, newDiff, "实际累计金额", oldRow.getActualCumulative(), newRow.getActualCumulative());
            compareNum(oldDiff, newDiff, "当月账面累计金额", oldRow.getBookCumulative(), newRow.getBookCumulative());
            compareNum(oldDiff, newDiff, "当月差异", oldRow.getDifference(), newRow.getDifference());
            compare(oldDiff, newDiff, "备注/服务期限", oldRow.getRemark(), newRow.getRemark());
            compare(oldDiff, newDiff, "数据核对人", oldRow.getVerifier(), newRow.getVerifier());

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

    private void compare(Map<String, String> oldDiff, Map<String, String> newDiff,
                         String label, String oldVal, String newVal) {
        String o = (oldVal != null) ? oldVal.trim() : "";
        String n = (newVal != null) ? newVal.trim() : "";
        if (!o.equals(n)) {
            oldDiff.put(label, o.isEmpty() ? "(空)" : o);
            newDiff.put(label, n.isEmpty() ? "(空)" : n);
        }
    }

    private void compareNum(Map<String, String> oldDiff, Map<String, String> newDiff,
                            String label, BigDecimal oldVal, BigDecimal newVal) {
        BigDecimal o = oldVal != null ? oldVal : BigDecimal.ZERO;
        BigDecimal n = newVal != null ? newVal : BigDecimal.ZERO;
        if (o.compareTo(n) != 0) {
            oldDiff.put(label, o.stripTrailingZeros().toPlainString());
            newDiff.put(label, n.stripTrailingZeros().toPlainString());
        }
    }

    private String toJson(BudgetItem item) {
        Map<String, String> m = new LinkedHashMap<>();
        put(m, "去向", item.getDirection());
        put(m, "项目", item.getProject());
        put(m, "片区名", item.getArea());
        put(m, "具体费用", item.getSpecificCost());
        put(m, "月份", item.getMonth());
        put(m, "实际累计金额", item.getActualCumulative());
        put(m, "当月账面累计金额", item.getBookCumulative());
        put(m, "当月差异", item.getDifference());
        put(m, "备注/服务期限", item.getRemark());
        put(m, "数据核对人", item.getVerifier());
        return m.toString();
    }

    private void put(Map<String, String> m, String key, String val) {
        if (val != null && !val.isEmpty()) m.put(key, val);
    }

    private void put(Map<String, String> m, String key, BigDecimal val) {
        if (val != null) m.put(key, val.toPlainString());
    }
}
