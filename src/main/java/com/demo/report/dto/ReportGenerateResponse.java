package com.demo.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportGenerateResponse {
    private Long reportId;
    private String status;     // READY
    private String fileName;
}
