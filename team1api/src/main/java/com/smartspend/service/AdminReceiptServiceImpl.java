package com.smartspend.service;

import com.smartspend.domain.ApprovalActionLog;
import com.smartspend.domain.ApprovalRequest;
import com.smartspend.domain.Expense;
import com.smartspend.domain.ReceiptVerification;
import com.smartspend.domain.Member;
import com.smartspend.dto.ReceiptDTO;
import com.smartspend.dto.ReceiptVerificationDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import com.smartspend.domain.ReceiptAiExtraction;
import com.smartspend.repository.ApprovalActionLogRepository;
import com.smartspend.repository.ApprovalRequestRepository;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.MemberRepository;
import com.smartspend.repository.ReceiptAiExtractionRepository;
import com.smartspend.repository.ReceiptUploadRepository;
import com.smartspend.repository.ReceiptVerificationRepository;
import com.smartspend.util.CustomFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class AdminReceiptServiceImpl implements AdminReceiptService {

    private final ReceiptUploadRepository receiptUploadRepository;
    private final ReceiptAiExtractionRepository receiptAiExtractionRepository;
    private final ExpenseRepository expenseRepository;
    private final ReceiptVerificationRepository receiptVerificationRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalActionLogRepository approvalActionLogRepository;
    private final MemberRepository memberRepository;
    private final CustomFileUtil customFileUtil;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ReceiptDTO> getList(PageRequestDTO pageRequestDTO, String status, Long approverId) {
        if (pageRequestDTO == null) {
            pageRequestDTO = PageRequestDTO.builder().page(1).size(15).build();
        }

        // 상신일 기준 정렬 (최근 상신일 먼저), 같은 날짜면 최근 업데이트 먼저
        Pageable pageable = pageRequestDTO.getPageable("createdAt", "updatedAt");

        log.info("getList..............");

        // Expense 테이블에서 제출된 지출 내역 조회 (영수증 유무와 관계없이)
        // DRAFT 상태는 제외 (아직 제출 안 된 것)
        Page<Expense> expensePage;
        if (status != null && !status.isEmpty()) {
            try {
                com.smartspend.domain.ApprovalStatus statusEnum = com.smartspend.domain.ApprovalStatus.valueOf(status);
                expensePage = expenseRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                // 잘못된 상태값이면 DRAFT 제외한 전체 조회
                expensePage = expenseRepository.findAllSubmitted(pageable);
            }
        } else {
            // 상태 필터가 없으면 DRAFT를 제외한 모든 제출된 지출 내역 조회
            expensePage = expenseRepository.findAllSubmitted(pageable);
        }

        List<Expense> expenses = expensePage.getContent();

        // 각 지출 내역을 ReceiptDTO로 변환 (영수증이 있으면 영수증 정보 포함, 없으면 null)
        List<ReceiptDTO> dtoList = expenses.stream()
                .map(this::expenseToDTO)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return PageResponseDTO.<ReceiptDTO>withAll()
                .dtoList(dtoList)
                .totalCount(expensePage.getTotalElements())
                .pageRequestDTO(pageRequestDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptDTO get(Long id) {
        com.smartspend.domain.ReceiptUpload receiptUpload = receiptUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));

        return entityToDTO(receiptUpload);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getImage(Long id) {
        com.smartspend.domain.ReceiptUpload receiptUpload = receiptUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));

        Path filePath = Paths.get(receiptUpload.getFileUrl());
        return customFileUtil.getFileAsResource(filePath);
    }

    @Override
    @Transactional(readOnly = true)
    public com.smartspend.dto.ReceiptExtractionDTO getExtraction(Long id) {
        com.smartspend.domain.ReceiptUpload receiptUpload = receiptUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));

        ReceiptAiExtraction extraction = receiptAiExtractionRepository.findByReceiptId(id)
                .orElseThrow(() -> new RuntimeException("AI 추출 결과를 찾을 수 없습니다."));

        return com.smartspend.dto.ReceiptExtractionDTO.builder()
                .receiptId(id)
                .modelName(extraction.getModelName())
                .extractedDate(extraction.getExtractedDate())
                .extractedAmount(extraction.getExtractedAmount())
                .extractedMerchant(extraction.getExtractedMerchant())
                .extractedCategory(extraction.getExtractedCategory())
                .confidence(extraction.getConfidence())
                .extractedJson(extraction.getExtractedJson())
                .createdAt(extraction.getCreatedAt())
                .build();
    }

    @Override
    public void verify(Long id, ReceiptVerificationDTO verificationDTO, Long adminId) {
        Expense expense;
        
        // 영수증이 있는 경우와 없는 경우 모두 처리
        if (id != null) {
            // 영수증이 있는 경우
            com.smartspend.domain.ReceiptUpload receiptUpload = receiptUploadRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));
            expense = receiptUpload.getExpense();
        } else {
            // 영수증이 없는 경우: expenseId로 직접 조회
            Long expenseId = verificationDTO.getExpenseId();
            if (expenseId == null) {
                throw new RuntimeException("expenseId가 필요합니다.");
            }
            expense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new RuntimeException("지출 내역을 찾을 수 없습니다."));
        }
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        if (!admin.isAdmin()) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }

        String action = verificationDTO.getAction();

        // Expense 상태 업데이트
        if ("APPROVE".equals(action)) {
            expense.approve();
            if (verificationDTO.getVerifiedMerchant() != null) {
                expense.setMerchant(verificationDTO.getVerifiedMerchant());
            }
            if (verificationDTO.getVerifiedAmount() != null) {
                expense.setAmount(verificationDTO.getVerifiedAmount());
            }
            if (verificationDTO.getVerifiedCategory() != null) {
                expense.setCategory(verificationDTO.getVerifiedCategory());
            }
        } else if ("REJECT".equals(action)) {
            expense.reject(verificationDTO.getReason());
        } else if ("REQUEST_MORE_INFO".equals(action)) {
            expense.requestMoreInfo(verificationDTO.getReason());
        }

        expenseRepository.save(expense);

        // ApprovalRequest 상태 동기화
        ApprovalRequest approvalRequest = approvalRequestRepository
                .findByRequestTypeAndRefId("EXPENSE", expense.getId())
                .orElse(null);

        if (approvalRequest != null) {
            approvalRequest.syncStatusSnapshot(expense.getStatus());
            approvalRequestRepository.save(approvalRequest);

            // ApprovalActionLog 생성
            ApprovalActionLog actionLog = ApprovalActionLog.builder()
                    .approvalRequest(approvalRequest)
                    .actor(admin)
                    .action(action)
                    .message(verificationDTO.getReason())
                    .build();

            approvalActionLogRepository.save(actionLog);
        }

        // ReceiptVerification 저장 (기존 검증이 있으면 삭제 후 새로 생성)
        receiptVerificationRepository.findByExpenseId(expense.getId())
                .ifPresent(receiptVerificationRepository::delete);

        ReceiptVerification verification = ReceiptVerification.builder()
                .expense(expense)
                .verifiedBy(admin)
                .verifiedMerchant(verificationDTO.getVerifiedMerchant())
                .verifiedAmount(verificationDTO.getVerifiedAmount())
                .verifiedCategory(verificationDTO.getVerifiedCategory())
                .reason(verificationDTO.getReason())
                .build();

        receiptVerificationRepository.save(verification);
    }

    private ReceiptDTO entityToDTO(com.smartspend.domain.ReceiptUpload entity) {
        Expense expense = entity.getExpense();
        Member uploadedBy = entity.getUploadedBy();
        
        if (expense == null || uploadedBy == null) {
            return null;
        }

        ReceiptDTO.ReceiptDTOBuilder builder = ReceiptDTO.builder()
                .id(entity.getId())
                .expenseId(expense.getId())
                .uploadedBy(uploadedBy.getId())
                .uploadedByName(uploadedBy.getName())
                .fileUrl(entity.getFileUrl())
                .fileHash(entity.getFileHash())
                .mimeType(entity.getMimeType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .status(expense.getStatus() != null ? expense.getStatus().name() : null);

        // AI 추출 결과 추가
        Optional<ReceiptAiExtraction> extractionOpt = receiptAiExtractionRepository.findByReceiptId(entity.getId());
        if (extractionOpt.isPresent()) {
            ReceiptAiExtraction extraction = extractionOpt.get();
            builder.extractionId(extraction.getId())
                    .modelName(extraction.getModelName())
                    .extractedJson(extraction.getExtractedJson())
                    .extractedDate(extraction.getExtractedDate())
                    .extractedAmount(extraction.getExtractedAmount())
                    .extractedMerchant(extraction.getExtractedMerchant())
                    .extractedCategory(extraction.getExtractedCategory())
                    .confidence(extraction.getConfidence())
                    .extractionCreatedAt(extraction.getCreatedAt());
        }

        // 검증 결과 추가
        receiptVerificationRepository.findByExpenseId(entity.getExpense().getId()).ifPresent(verification -> {
            Member verifiedBy = verification.getVerifiedBy();
            if (verifiedBy != null) {
                builder.verificationId(verification.getId())
                        .verifiedBy(verifiedBy.getId())
                        .verifiedByName(verifiedBy.getName())
                        .verifiedMerchant(verification.getVerifiedMerchant())
                        .verifiedAmount(verification.getVerifiedAmount())
                        .verifiedCategory(verification.getVerifiedCategory())
                        .reason(verification.getReason())
                        .verificationCreatedAt(verification.getCreatedAt());
            } else {
                log.warn("⚠️ ReceiptVerification ID {}의 verifiedBy가 null입니다.", verification.getId());
            }
        });

        return builder.build();
    }

    // Expense를 ReceiptDTO로 변환 (영수증이 없어도 처리 가능)
    private ReceiptDTO expenseToDTO(Expense expense) {
        Member writer = expense.getWriter();
        
        if (writer == null || expense.getStatus() == null) {
            return null;
        }

        // 영수증이 있는지 확인
        Optional<com.smartspend.domain.ReceiptUpload> receiptOpt = receiptUploadRepository.findByExpenseId(expense.getId());

        ReceiptDTO.ReceiptDTOBuilder builder = ReceiptDTO.builder()
                .expenseId(expense.getId())
                .uploadedBy(writer.getId())
                .uploadedByName(writer.getName())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .status(expense.getStatus().name());

        // 영수증이 있으면 영수증 정보 포함
        if (receiptOpt.isPresent()) {
            com.smartspend.domain.ReceiptUpload receipt = receiptOpt.get();
            builder.id(receipt.getId())
                    .fileUrl(receipt.getFileUrl())
                    .fileHash(receipt.getFileHash())
                    .mimeType(receipt.getMimeType());

            // AI 추출 결과 추가
            Optional<ReceiptAiExtraction> extractionOpt = receiptAiExtractionRepository.findByReceiptId(receipt.getId());
            if (extractionOpt.isPresent()) {
                ReceiptAiExtraction extraction = extractionOpt.get();
                builder.extractionId(extraction.getId())
                        .modelName(extraction.getModelName())
                        .extractedJson(extraction.getExtractedJson())
                        .extractedDate(extraction.getExtractedDate())
                        .extractedAmount(extraction.getExtractedAmount())
                        .extractedMerchant(extraction.getExtractedMerchant())
                        .extractedCategory(extraction.getExtractedCategory())
                        .confidence(extraction.getConfidence())
                        .extractionCreatedAt(extraction.getCreatedAt());
            }
        } else {
            // 영수증이 없으면 id는 null, fileUrl 등도 null
            builder.id(null)
                    .fileUrl(null)
                    .fileHash(null)
                    .mimeType(null);
        }

        // 검증 결과 추가 (영수증 유무와 관계없이)
        receiptVerificationRepository.findByExpenseId(expense.getId()).ifPresent(verification -> {
            Member verifiedBy = verification.getVerifiedBy();
            if (verifiedBy != null) {
                builder.verificationId(verification.getId())
                        .verifiedBy(verifiedBy.getId())
                        .verifiedByName(verifiedBy.getName())
                        .verifiedMerchant(verification.getVerifiedMerchant())
                        .verifiedAmount(verification.getVerifiedAmount())
                        .verifiedCategory(verification.getVerifiedCategory())
                        .reason(verification.getReason())
                        .verificationCreatedAt(verification.getCreatedAt());
            }
        });

        return builder.build();
    }
}

