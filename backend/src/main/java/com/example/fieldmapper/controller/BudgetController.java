package com.example.fieldmapper.controller;

import com.example.fieldmapper.model.BudgetItem;
import com.example.fieldmapper.service.AuditService;
import com.example.fieldmapper.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
        auditService.logRow("INSERT", newId, null, item, username);
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
        auditService.logRow("UPDATE", id, old, item, auth.getName());
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }
}
