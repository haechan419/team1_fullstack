package com.smartspend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportJobDTO {
    private Long id;
    private Long requestedBy;
    private String requestedByName;
    private String scopeType;
    private String scopeValue;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String template;
    private String reportType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 리포트 파일 정보
    private String fileName;
    private String fileUrl;
    private String fileType;
}

