package com.mysocial.repository;

import com.mysocial.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // Có thể thêm các phương thức filter nếu cần
} 