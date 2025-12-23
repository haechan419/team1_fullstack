package com.demo.report.dto;

public record ReportFileItem(
        Long fileId,
        String fileName,
        String fileType,
        Long fileSize,
        String checksum,
        String fileUrl,
        java.time.LocalDateTime createdAt
) {}