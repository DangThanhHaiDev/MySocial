package com.mysocial.controller;

import com.mysocial.dto.PagedResponse;
import com.mysocial.model.Report;
import com.mysocial.model.User;
import com.mysocial.repository.UserRepository;
import com.mysocial.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportService reportService;

    // 1. Lấy danh sách user
    @GetMapping("/users")
    public PagedResponse<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Page<User> userPage;
        if (status != null && !status.isEmpty()) {
            userPage = userRepository.findByStatus(status, PageRequest.of(page, size));
        } else {
            userPage = userRepository.findAll(PageRequest.of(page, size));
        }
        return new PagedResponse<>(userPage);
    }

    // 2. Khóa tài khoản
    @PutMapping("/users/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus("BANNED");
            userRepository.save(user);
            return ResponseEntity.ok("User banned");
        }
        return ResponseEntity.notFound().build();
    }

    // 3. Mở khóa tài khoản
    @PutMapping("/users/{id}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatus("ACTIVE");
            userRepository.save(user);
            return ResponseEntity.ok("User unbanned");
        }
        return ResponseEntity.notFound().build();
    }

    // 4. Xóa tài khoản
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted");
        }
        return ResponseEntity.notFound().build();
    }

    // 5. Lấy danh sách báo cáo
    @GetMapping("/reports")
    public Page<Report> getAllReports(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return reportService.getAllReports(PageRequest.of(page, size));
    }

    // 6. Xem chi tiết báo cáo
    @GetMapping("/reports/{id}")
    public ResponseEntity<Report> getReport(@PathVariable Long id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 7. Xử lý báo cáo
    @PutMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Long id, @RequestBody ResolveReportRequest req) {
        Optional<Report> reportOpt = reportService.getReportById(id);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            report.setStatus("RESOLVED");
            report.setHandledBy(req.getAdminId());
            report.setHandledAt(LocalDateTime.now());
            report.setResult(req.getResult());
            reportService.saveReport(report);
            // Có thể thêm logic xử lý: xóa bài, khóa user...
            return ResponseEntity.ok("Report resolved");
        }
        return ResponseEntity.notFound().build();
    }

    // DTO cho xử lý báo cáo
    public static class ResolveReportRequest {
        private Long adminId;
        private String result;
        public Long getAdminId() { return adminId; }
        public void setAdminId(Long adminId) { this.adminId = adminId; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
    }
} 