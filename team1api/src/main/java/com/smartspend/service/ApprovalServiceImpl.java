package com.smartspend.service;

import com.smartspend.domain.ApprovalActionLog;
import com.smartspend.domain.ApprovalRequest;
import com.smartspend.domain.ApprovalStatus;
import com.smartspend.domain.Expense;
import com.smartspend.domain.Member;
import com.smartspend.dto.ApprovalActionDTO;
import com.smartspend.dto.ApprovalActionLogDTO;
import com.smartspend.dto.ApprovalRequestDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import com.smartspend.repository.ApprovalActionLogRepository;
import com.smartspend.repository.ApprovalRequestRepository;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalActionLogRepository approvalActionLogRepository;
    private final ExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;

    @Override
    public PageResponseDTO<ApprovalRequestDTO> getList(Long userId, boolean isAdmin, PageRequestDTO pageRequestDTO, String requestType, String status, LocalDate startDate, LocalDate endDate) {
        // 상신일 기준 정렬을 위한 Pageable (정렬은 Native Query에서 처리하므로 Pageable은 페이징만)
        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize());

        String statusString = null;
        if (status != null && !status.isEmpty()) {
            try {
                ApprovalStatus.valueOf(status); // 유효성 검증용
                statusString = status; // Native Query용
            } catch (IllegalArgumentException e) {
                // 잘못된 status 값은 무시
            }
        }

        Page<ApprovalRequest> result;
        if (isAdmin) {
            // 관리자는 모든 요청 조회 (DRAFT 제외 - DRAFT는 ApprovalRequest가 없음)
            // 상신일 기준 정렬 사용 (Native Query 사용)
            boolean hasDateFilter = startDate != null && endDate != null;
            
            if (hasDateFilter) {
                // 상신일 필터가 있는 경우
                if (requestType != null && !requestType.isEmpty() && statusString != null) {
                    result = approvalRequestRepository.findByRequestTypeAndStatusSnapshotAndDateRange(requestType, statusString, startDate, endDate, pageable);
                } else if (requestType != null && !requestType.isEmpty()) {
                    result = approvalRequestRepository.findByRequestTypeAndDateRange(requestType, startDate, endDate, pageable);
                } else if (statusString != null) {
                    result = approvalRequestRepository.findByStatusSnapshotAndDateRange(statusString, startDate, endDate, pageable);
                } else {
                    result = approvalRequestRepository.findAllByDateRange(startDate, endDate, pageable);
                }
            } else {
                // 상신일 필터가 없는 경우
                if (requestType != null && !requestType.isEmpty() && statusString != null) {
                    result = approvalRequestRepository.findByRequestTypeAndStatusSnapshotOrderByReceiptDate(requestType, statusString, pageable);
                } else if (requestType != null && !requestType.isEmpty()) {
                    result = approvalRequestRepository.findByRequestTypeOrderByReceiptDate(requestType, pageable);
                } else if (statusString != null) {
                    result = approvalRequestRepository.findByStatusSnapshotOrderByReceiptDate(statusString, pageable);
                } else {
                    // status 필터가 없으면 모든 상태 조회 (DRAFT 제외)
                    result = approvalRequestRepository.findAllOrderByReceiptDate(pageable);
                }
            }
        } else {
            // 일반 직원은 본인 요청만 조회
            // 상신일 기준 정렬 사용 (Native Query 사용)
            // 일반 직원용 상신일 필터는 향후 필요시 추가
            if (statusString != null) {
                result = approvalRequestRepository.findByRequesterIdAndStatusSnapshotOrderByReceiptDate(userId, statusString, pageable);
            } else {
                result = approvalRequestRepository.findByRequesterIdOrderByReceiptDate(userId, pageable);
            }
        }

        List<ApprovalRequestDTO> dtoList = result.getContent().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

        long totalCount = result.getTotalElements();
        
        // DRAFT 상태는 결재 관리에서 제외 (아직 제출되지 않은 상태이므로)
        // DRAFT는 "내 지출 내역" 페이지에서만 관리

        return PageResponseDTO.<ApprovalRequestDTO>withAll()
                .dtoList(dtoList)
                .totalCount(totalCount)
                .pageRequestDTO(pageRequestDTO)
                .build();
    }

    @Override
    public ApprovalRequestDTO get(Long id, Long userId, boolean isAdmin) {
        // requester, approver를 함께 로드 (LAZY 로딩 방지)
        ApprovalRequest approvalRequest = approvalRequestRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new RuntimeException("결재 요청을 찾을 수 없습니다."));

        // 권한 확인
        if (!isAdmin && !approvalRequest.getRequester().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        return entityToDTO(approvalRequest);
    }

    @Override
    public List<ApprovalActionLogDTO> getLogs(Long id, Long userId, boolean isAdmin) {
        // requester, approver를 함께 로드 (LAZY 로딩 방지)
        ApprovalRequest approvalRequest = approvalRequestRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new RuntimeException("결재 요청을 찾을 수 없습니다."));

        // 권한 확인
        if (!isAdmin && !approvalRequest.getRequester().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        List<ApprovalActionLog> logs = approvalActionLogRepository.findByApprovalRequestIdOrderByCreatedAtAsc(id);

        return logs.stream()
                .map(this::logEntityToDTO)
                .collect(Collectors.toList());
    }

    private ApprovalRequestDTO entityToDTO(ApprovalRequest entity) {
        ApprovalRequestDTO.ApprovalRequestDTOBuilder builder = ApprovalRequestDTO.builder()
                .id(entity.getId())
                .requestType(entity.getRequestType())
                .refId(entity.getRefId())
                .requesterId(entity.getRequester().getId())
                .requesterName(entity.getRequester().getName())
                .statusSnapshot(entity.getStatusSnapshot().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getApprover() != null) {
            builder.approverId(entity.getApprover().getId())
                    .approverName(entity.getApprover().getName());
        }

        return builder.build();
    }

    @Override
    @Transactional
    public ApprovalRequestDTO action(Long id, ApprovalActionDTO actionDTO, Long adminId) {
        try {
            log.info("🔍 결재 처리 시작 - id: {}, adminId: {}, actionDTO: {}", id, adminId, actionDTO);
            
            if (id == null) {
                log.error("❌ 결재 요청 ID가 null입니다.");
                throw new RuntimeException("결재 요청 ID가 필요합니다.");
            }
            
            if (actionDTO == null) {
                log.error("❌ ApprovalActionDTO가 null입니다.");
                throw new RuntimeException("결재 처리 정보가 필요합니다.");
            }
            
            // requester, approver를 함께 로드 (LAZY 로딩 방지)
            ApprovalRequest approvalRequest = approvalRequestRepository.findByIdWithRelations(id)
                    .orElseThrow(() -> {
                        log.error("❌ 결재 요청을 찾을 수 없습니다. id: {}", id);
                        return new RuntimeException("결재 요청을 찾을 수 없습니다.");
                    });

            Member admin = memberRepository.findById(adminId)
                    .orElseThrow(() -> {
                        log.error("❌ 관리자를 찾을 수 없습니다. adminId: {}", adminId);
                        return new RuntimeException("관리자를 찾을 수 없습니다.");
                    });

            // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
            boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
            
            if (!isAdmin) {
                log.warn("❌ 관리자 권한이 없습니다. adminId: {}, isAdmin: {}", adminId, admin.isAdmin());
                throw new RuntimeException("관리자 권한이 필요합니다.");
            }

            String action = actionDTO.getAction();
            String message = actionDTO.getMessage();
            
            log.info("📝 결재 처리 정보 - action: {}, message: {}, requestType: {}", 
                    action, message, approvalRequest.getRequestType());

            // requestType에 따라 분기 처리
            if ("EXPENSE".equals(approvalRequest.getRequestType())) {
                // 지출 내역 처리 (관리자용이므로 findByIdWithWriter 사용)
                Expense expense = expenseRepository.findByIdWithWriter(approvalRequest.getRefId())
                        .orElseThrow(() -> {
                            log.error("❌ 지출 내역을 찾을 수 없습니다. refId: {}", approvalRequest.getRefId());
                            return new RuntimeException("지출 내역을 찾을 수 없습니다.");
                        });

                log.info("📋 지출 내역 조회 완료 - expenseId: {}, 현재 상태: {}", expense.getId(), expense.getStatus());

                // Expense 상태 업데이트
                if ("APPROVE".equals(action)) {
                    expense.approve();
                    log.info("✅ 승인 처리 - expenseId: {}", expense.getId());
                } else if ("REJECT".equals(action)) {
                    expense.reject(message);
                    log.info("❌ 반려 처리 - expenseId: {}, message: {}", expense.getId(), message);
                } else if ("REQUEST_MORE_INFO".equals(action)) {
                    expense.requestMoreInfo(message);
                    log.info("📝 보완 요청 처리 - expenseId: {}, message: {}", expense.getId(), message);
                } else {
                    log.error("❌ 지원하지 않는 액션입니다. action: {}", action);
                    throw new RuntimeException("지원하지 않는 액션입니다: " + action);
                }

                expenseRepository.save(expense);
                log.info("💾 Expense 저장 완료 - expenseId: {}, 새 상태: {}", expense.getId(), expense.getStatus());

                // ApprovalRequest 상태 동기화
                approvalRequest.syncStatusSnapshot(expense.getStatus());
                approvalRequestRepository.save(approvalRequest);
                log.info("💾 ApprovalRequest 저장 완료 - approvalRequestId: {}, 새 상태: {}", 
                        approvalRequest.getId(), approvalRequest.getStatusSnapshot());

                // ApprovalActionLog 생성
                ApprovalActionLog actionLog = ApprovalActionLog.builder()
                        .approvalRequest(approvalRequest)
                        .actor(admin)
                        .action(action)
                        .message(message)
                        .build();

                approvalActionLogRepository.save(actionLog);
                log.info("✅ ApprovalActionLog 생성 완료 - action: {}, approvalRequestId: {}, actorId: {}", 
                        action, approvalRequest.getId(), admin.getId());

            } else if ("STORE_ORDER".equals(approvalRequest.getRequestType())) {
                // 비품 요청 처리 (3번 파트에서 구현 예정)
                log.error("❌ 비품 요청(STORE_ORDER)은 아직 지원하지 않습니다.");
                throw new RuntimeException("비품 요청(STORE_ORDER)은 아직 지원하지 않습니다.");
            } else {
                log.error("❌ 지원하지 않는 요청 타입입니다. requestType: {}", approvalRequest.getRequestType());
                throw new RuntimeException("지원하지 않는 요청 타입입니다: " + approvalRequest.getRequestType());
            }

            log.info("✅ 결재 처리 완료 - id: {}, action: {}", id, action);
            return entityToDTO(approvalRequest);
            
        } catch (RuntimeException e) {
            log.error("❌ 결재 처리 실패 - id: {}, error: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("❌ 결재 처리 중 예상치 못한 오류 발생 - id: {}", id, e);
            throw new RuntimeException("결재 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private ApprovalActionLogDTO logEntityToDTO(ApprovalActionLog entity) {
        if (entity == null) {
            return null;
        }
        
        ApprovalActionLogDTO.ApprovalActionLogDTOBuilder builder = ApprovalActionLogDTO.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt());
        
        if (entity.getApprovalRequest() != null) {
            builder.approvalRequestId(entity.getApprovalRequest().getId());
        }
        
        if (entity.getActor() != null) {
            builder.actorId(entity.getActor().getId())
                   .actorName(entity.getActor().getName());
        }
        
        return builder.build();
    }
}

