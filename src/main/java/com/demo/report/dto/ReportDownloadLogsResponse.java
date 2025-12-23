package com.demo.report.dto;

import java.util.List;

public record ReportDownloadLogsResponse(
        Long reportId,
        List<ReportDownloadLogItem> items
) {}
