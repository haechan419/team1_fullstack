package com.smartspend.service;

import com.smartspend.dto.ReportJobDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;

public interface ReportService {
    Long generateReport(ReportJobDTO reportJobDTO, Long userId);
    PageResponseDTO<ReportJobDTO> getReportList(Long userId, PageRequestDTO pageRequestDTO);
    ReportJobDTO getReport(Long reportId, Long userId);
    byte[] downloadReport(Long reportId, Long userId);
}

