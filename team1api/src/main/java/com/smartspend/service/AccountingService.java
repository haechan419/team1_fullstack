package com.smartspend.service;

import com.smartspend.dto.DepartmentStatisticsDTO;

import java.util.List;
import java.util.Map;

public interface AccountingService {
    List<DepartmentStatisticsDTO> getDepartmentStatistics(String status);
    List<String> getDepartments();
    List<Map<String, Object>> getCategoryStatistics(String status);
    Map<String, Object> getSummary();
    List<Map<String, Object>> getOverBudgetList();
}

