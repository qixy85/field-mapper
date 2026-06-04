package com.example.fieldmapper.controller;

import com.example.fieldmapper.model.AuditLog;
import com.example.fieldmapper.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    public List<AuditLog> listLogs() {
        return auditService.findAll();
    }
}
