package com.demo.report.dto;


public record ReportFilesResponse(
        Long reportId,
        java.util.List<ReportFileItem> files
) {}
