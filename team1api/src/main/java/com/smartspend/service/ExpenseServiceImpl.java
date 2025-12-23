package com.smartspend.service;

import com.smartspend.domain.ApprovalActionLog;
import com.smartspend.domain.ApprovalRequest;
import com.smartspend.domain.Expense;
import com.smartspend.domain.ReceiptUpload;
import com.smartspend.domain.ApprovalStatus;
import com.smartspend.domain.Member;
import com.smartspend.dto.ExpenseDTO;
import com.smartspend.dto.ExpenseSubmitDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import com.smartspend.repository.ApprovalActionLogRepository;
import com.smartspend.repository.ApprovalRequestRepository;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.MemberRepository;
import com.smartspend.repository.ReceiptUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;
    private final ReceiptUploadRepository receiptUploadRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalActionLogRepository approvalActionLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ExpenseDTO> getList(Long userId, PageRequestDTO pageRequestDTO, String status, LocalDate startDate, LocalDate endDate) {
        if (pageRequestDTO == null) {
            pageRequestDTO = PageRequestDTO.builder().page(1).size(15).build();
        }

        // 상신일 기준 정렬 (최근 상신일 먼저), 같은 날짜면 최근 업데이트 먼저
        Pageable pageable = pageRequestDTO.getPageable("createdAt", "updatedAt");

        ApprovalStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = ApprovalStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ 잘못된 status 값: {}", status);
                // 잘못된 status 값은 무시
            }
        }

        Page<Expense> result;
        if (statusEnum != null && startDate != null && endDate != null) {
            result = expenseRepository.findByUserIdAndStatusAndDateRange(userId, statusEnum, startDate, endDate, pageable);
        } else if (statusEnum != null) {
            result = expenseRepository.findByWriterIdAndStatus(userId, statusEnum, pageable);
        } else if (startDate != null && endDate != null) {
            result = expenseRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable);
        } else {
            result = expenseRepository.findByWriterId(userId, pageable);
        }

        log.info("getList..............");

        List<ExpenseDTO> dtoList = result.getContent().stream()
                .map(this::entityToDTO)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return PageResponseDTO.<ExpenseDTO>withAll()
                .dtoList(dtoList)
                .totalCount(result.getTotalElements())
                .pageRequestDTO(pageRequestDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDTO get(Long id, Long userId, boolean isAdmin) {
        Optional<Expense> result;
        if (isAdmin) {
            // 관리자는 모든 지출 내역 조회 가능 (writer 정보 포함)
            result = expenseRepository.findByIdWithWriter(id);
        } else {
            // 일반 사용자는 본인 지출 내역만 조회
            result = expenseRepository.findByIdAndWriterId(id, userId);
        }
        Expense expense = result.orElseThrow(() -> new RuntimeException("지출 내역을 찾을 수 없습니다."));
        return entityToDTO(expense);
    }

    @Override
    public Long register(ExpenseDTO expenseDTO, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Expense expense = dtoToEntity(expenseDTO);
        expense.setWriter(member);
        expense.setStatus(ApprovalStatus.DRAFT);

        Expense saved = expenseRepository.save(expense);
        return saved.getId();
    }

    @Override
    public void modify(ExpenseDTO expenseDTO, Long userId) {
        Optional<Expense> result = expenseRepository.findByIdAndWriterId(expenseDTO.getId(), userId);
        Expense expense = result.orElseThrow(() -> new RuntimeException("지출 내역을 찾을 수 없습니다."));

        if (!expense.canModify()) {
            throw new IllegalStateException("수정할 수 없는 상태입니다.");
        }

        expense.setReceiptDate(expenseDTO.getReceiptDate());
        expense.setMerchant(expenseDTO.getMerchant());
        expense.setAmount(expenseDTO.getAmount());
        expense.setCategory(expenseDTO.getCategory());
        expense.setDescription(expenseDTO.getDescription());

        expenseRepository.save(expense);
    }

    @Override
    public void remove(Long id, Long userId) {
        Optional<Expense> result = expenseRepository.findByIdAndWriterId(id, userId);
        Expense expense = result.orElseThrow(() -> new RuntimeException("지출 내역을 찾을 수 없습니다."));

        if (!expense.canDelete()) {
            throw new IllegalStateException("삭제할 수 없는 상태입니다.");
        }

        expenseRepository.delete(expense);
    }

    @Override
    public void submit(Long id, Long userId, ExpenseSubmitDTO submitDTO) {
        Optional<Expense> result = expenseRepository.findByIdAndWriterId(id, userId);
        Expense expense = result.orElseThrow(() -> new RuntimeException("지출 내역을 찾을 수 없습니다."));

        expense.submit();
        if (submitDTO != null && submitDTO.getRequestNote() != null) {
            expense.setDescription(submitDTO.getRequestNote());
        }

        expenseRepository.save(expense);

        // ApprovalRequest 생성
        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .requestType("EXPENSE")
                .refId(expense.getId())
                .requester(expense.getWriter())
                .statusSnapshot(ApprovalStatus.SUBMITTED)
                .build();

        ApprovalRequest savedRequest = approvalRequestRepository.save(approvalRequest);

        // ApprovalActionLog 생성 (SUBMIT 액션)
        String message = (submitDTO != null && submitDTO.getRequestNote() != null) 
            ? submitDTO.getRequestNote() 
            : "지출 내역을 제출했습니다.";
        
        ApprovalActionLog actionLog = ApprovalActionLog.builder()
                .approvalRequest(savedRequest)
                .actor(expense.getWriter())
                .action("SUBMIT")
                .message(message)
                .build();

        approvalActionLogRepository.save(actionLog);
    }

    private Expense dtoToEntity(ExpenseDTO dto) {
        return Expense.builder()
                .receiptDate(dto.getReceiptDate())
                .merchant(dto.getMerchant())
                .amount(dto.getAmount())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .status(ApprovalStatus.DRAFT)
                .build();
    }

    private ExpenseDTO entityToDTO(Expense entity) {
        if (entity == null) {
            log.warn("⚠️ Expense entity가 null입니다.");
            return null;
        }
        
        Member writer = entity.getWriter();
        ApprovalStatus status = entity.getStatus();
        
        if (writer == null) {
            log.warn("⚠️ Expense ID {}의 writer가 null입니다.", entity.getId());
            return null;
        }
        
        if (status == null) {
            log.warn("⚠️ Expense ID {}의 status가 null입니다.", entity.getId());
            return null;
        }

        ExpenseDTO.ExpenseDTOBuilder builder = ExpenseDTO.builder()
                .id(entity.getId())
                .userId(writer.getId())
                .userName(writer.getName())
                .receiptDate(entity.getReceiptDate())
                .merchant(entity.getMerchant())
                .amount(entity.getAmount())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .status(status.name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // 영수증 정보 추가
        Optional<ReceiptUpload> receiptOpt = receiptUploadRepository.findByExpenseId(entity.getId());
        if (receiptOpt.isPresent()) {
            ReceiptUpload receipt = receiptOpt.get();
            builder.receiptId(receipt.getId())
                    .receiptFileUrl(receipt.getFileUrl())
                    .hasReceipt(true);
        } else {
            builder.hasReceipt(false);
        }

        // receipt_image_url도 추가
        if (entity.getReceiptImageUrl() != null) {
            builder.receiptImageUrl(entity.getReceiptImageUrl());
        }

        return builder.build();
    }
}

