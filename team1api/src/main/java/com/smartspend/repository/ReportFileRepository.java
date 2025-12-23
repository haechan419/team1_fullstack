package com.smartspend.repository;

import com.smartspend.domain.ReportFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportFileRepository extends JpaRepository<ReportFile, Long> {

    List<ReportFile> findByReportJobId(Long reportJobId);

    Optional<ReportFile> findFirstByReportJobIdOrderByCreatedAtDesc(Long reportJobId);
}

