package com.mysocial.controller;

import com.mysocial.dto.CreateReportRequest;
import com.mysocial.model.Report;
import com.mysocial.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody CreateReportRequest req) {
        Report report = new Report();
        report.setType(req.getType());
        report.setTargetId(req.getTargetId());
        report.setReason(req.getReason());
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());
        reportService.saveReport(report);
        return ResponseEntity.ok("Đã gửi báo cáo");
    }
} 