package com.demo.report.dto;

import java.time.LocalDateTime;

public record ReportDownloadLogItem(
        Long logId,
        Long fileId,
        String fileName,
        Long downloadedBy,
        LocalDateTime downloadedAt
) {}
