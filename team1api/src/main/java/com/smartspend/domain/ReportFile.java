package com.smartspend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_file", indexes = {
    @Index(name = "idx_report_job_id", columnList = "report_job_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_job_id", nullable = false)
    private ReportJob reportJob;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_url", length = 255, nullable = false)
    private String fileUrl;

    @Column(name = "file_type", length = 20, nullable = false)
    private String fileType; // XLSX, PDF

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

