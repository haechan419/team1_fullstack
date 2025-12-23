package com.smartspend.repository;

import com.smartspend.domain.ReportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportJobRepository extends JpaRepository<ReportJob, Long> {

    @Query("SELECT rj FROM ReportJob rj WHERE rj.requestedBy.id = :userId ORDER BY rj.createdAt DESC")
    Page<ReportJob> findByRequestedBy(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT rj FROM ReportJob rj WHERE rj.status = :status ORDER BY rj.createdAt DESC")
    List<ReportJob> findByStatus(@Param("status") String status);
}

