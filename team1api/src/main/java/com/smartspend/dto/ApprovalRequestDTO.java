package com.smartspend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalRequestDTO {

    private Long id;

    private String requestType; // EXPENSE 또는 STORE_ORDER

    private Long refId;

    private Long requesterId;

    private String requesterName;

    private Long approverId;

    private String approverName;

    private String statusSnapshot;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

