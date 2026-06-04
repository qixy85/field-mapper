package com.example.fieldmapper.controller;

import com.example.fieldmapper.model.BudgetItem;
import com.example.fieldmapper.service.AuditService;
import com.example.fieldmapper.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;
    private final AuditService auditService;

    public BudgetController(BudgetService budgetService, AuditService auditService) {
        this.budgetService = budgetService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<BudgetItem> list() {
        return budgetService.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BudgetItem item, Authentication auth) {
        String username = auth.getName();
        item.setCreatedBy(username);
        Long newId = budgetService.insert(item);
        item.setId(newId);
        auditAllFields("INSERT", item, null, username);
        return ResponseEntity.ok(Map.of("id", newId, "message", "创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody BudgetItem item, Authentication auth) {
        BudgetItem old = budgetService.findById(id);
        if (old == null) {
            return ResponseEntity.status(404).body(Map.of("error", "记录不存在"));
        }
        item.setId(id);
        item.setCreatedBy(old.getCreatedBy());
        budgetService.update(item);
        auditAllFields("UPDATE", item, old, auth.getName());
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    private void auditAllFields(String action, BudgetItem current, BudgetItem old, String username) {
        String table = "BUDGET_ITEMS";
        Long recordId = current.getId();
        auditField(table, recordId, action, "DIRECTION", old != null ? old.getDirection() : null, current.getDirection(), username);
        auditField(table, recordId, action, "PROJECT", old != null ? old.getProject() : null, current.getProject(), username);
        auditField(table, recordId, action, "AREA", old != null ? old.getArea() : null, current.getArea(), username);
        auditField(table, recordId, action, "SPECIFIC_COST", old != null ? old.getSpecificCost() : null, current.getSpecificCost(), username);
        auditField(table, recordId, action, "MONTH", old != null ? old.getMonth() : null, current.getMonth(), username);
        auditField(table, recordId, action, "ACTUAL_CUMULATIVE", old != null ? val(old.getActualCumulative()) : null, val(current.getActualCumulative()), username);
        auditField(table, recordId, action, "BOOK_CUMULATIVE", old != null ? val(old.getBookCumulative()) : null, val(current.getBookCumulative()), username);
        auditField(table, recordId, action, "DIFFERENCE", old != null ? val(old.getDifference()) : null, val(current.getDifference()), username);
        auditField(table, recordId, action, "REMARK", old != null ? old.getRemark() : null, current.getRemark(), username);
        auditField(table, recordId, action, "VERIFIER", old != null ? old.getVerifier() : null, current.getVerifier(), username);
    }

    private void auditField(String table, Long recordId, String action, String field,
                            String oldVal, String newVal, String username) {
        if ("INSERT".equals(action)) {
            if (newVal != null && !newVal.isEmpty()) {
                auditService.log(table, recordId, action, field, "", newVal, username);
            }
        } else {
            String o = oldVal != null ? oldVal : "";
            String n = newVal != null ? newVal : "";
            if (!o.equals(n)) {
                auditService.log(table, recordId, action, field, o, n, username);
            }
        }
    }

    private String val(BigDecimal bd) {
        return bd != null ? bd.toPlainString() : "";
    }
}
