package com.demo.report.dto;

import java.util.List;

public record ReportFileDownloadLogsResponse(
        Long fileId,
        List<ReportDownloadLogItem> items
) {}
