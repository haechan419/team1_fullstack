package com.smartspend.service;

import com.smartspend.dto.ApprovalActionDTO;
import com.smartspend.dto.ApprovalActionLogDTO;
import com.smartspend.dto.ApprovalRequestDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ApprovalService {

    PageResponseDTO<ApprovalRequestDTO> getList(Long userId, boolean isAdmin, PageRequestDTO pageRequestDTO, String requestType, String status, LocalDate startDate, LocalDate endDate);

    ApprovalRequestDTO get(Long id, Long userId, boolean isAdmin);

    List<ApprovalActionLogDTO> getLogs(Long id, Long userId, boolean isAdmin);

    ApprovalRequestDTO action(Long id, ApprovalActionDTO actionDTO, Long adminId);
}

