package com.demo.report.repository;

import com.demo.report.entity.ReportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportJobRepository extends JpaRepository<ReportJob, Long> {

    Optional<ReportJob> findById(Long id);

}
