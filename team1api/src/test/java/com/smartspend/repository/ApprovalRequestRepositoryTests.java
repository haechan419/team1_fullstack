package com.smartspend.repository;

import com.smartspend.domain.ApprovalRequest;
import com.smartspend.domain.ApprovalStatus;
import com.smartspend.domain.Expense;
import com.smartspend.domain.Member;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
public class ApprovalRequestRepositoryTests {

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    public void testInsert() {
        // given
        Member requester = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        Member approver = memberRepository.findByEmployeeNo("ADMIN001")
                .orElseGet(() -> requester); // ADMIN001이 없으면 requester 사용

        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .requestType("EXPENSE")
                .refId(1L)
                .requester(requester)
                .approver(approver)
                .statusSnapshot(ApprovalStatus.SUBMITTED)
                .build();

        // when
        ApprovalRequest saved = approvalRequestRepository.save(approvalRequest);

        // then
        assertNotNull(saved.getId());
        log.info("저장된 결재 요청 ID: {}", saved.getId());
        log.info("저장된 결재 요청: {}", saved);
    }

    @Test
    @Transactional
    public void testFindByRequesterId() {
        // given
        Member requester = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<ApprovalRequest> result = approvalRequestRepository.findByRequesterId(
                requester.getId(),
                pageable
        );

        // then
        assertNotNull(result);
        log.info("요청자별 결재 요청 개수: {}", result.getTotalElements());
        result.getContent().forEach(request -> {
            assertEquals(requester.getId(), request.getRequester().getId());
            log.info("결재 요청: {}", request);
        });
    }

    @Test
    @Transactional
    public void testFindByRequesterIdAndStatusSnapshot() {
        // given
        Member requester = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<ApprovalRequest> result = approvalRequestRepository.findByRequesterIdAndStatusSnapshot(
                requester.getId(),
                ApprovalStatus.APPROVED,
                pageable
        );

        // then
        assertNotNull(result);
        log.info("APPROVED 상태 결재 요청 개수: {}", result.getTotalElements());
        result.getContent().forEach(request -> {
            assertEquals(ApprovalStatus.APPROVED, request.getStatusSnapshot());
            assertEquals(requester.getId(), request.getRequester().getId());
            log.info("결재 요청: {}", request);
        });
    }

    @Test
    @Transactional
    public void testFindByRequestType() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<ApprovalRequest> result = approvalRequestRepository.findByRequestType(
                "EXPENSE",
                pageable
        );

        // then
        assertNotNull(result);
        log.info("EXPENSE 타입 결재 요청 개수: {}", result.getTotalElements());
        result.getContent().forEach(request -> {
            assertEquals("EXPENSE", request.getRequestType());
            log.info("결재 요청: {}", request);
        });
    }

    @Test
    @Transactional
    public void testFindByStatusSnapshot() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<ApprovalRequest> result = approvalRequestRepository.findByStatusSnapshot(
                ApprovalStatus.SUBMITTED,
                pageable
        );

        // then
        assertNotNull(result);
        log.info("SUBMITTED 상태 결재 요청 개수: {}", result.getTotalElements());
        result.getContent().forEach(request -> {
            assertEquals(ApprovalStatus.SUBMITTED, request.getStatusSnapshot());
            log.info("결재 요청: {}", request);
        });
    }

    @Test
    @Transactional
    public void testFindByRequestTypeAndStatusSnapshot() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<ApprovalRequest> result = approvalRequestRepository.findByRequestTypeAndStatusSnapshot(
                "EXPENSE",
                ApprovalStatus.APPROVED,
                pageable
        );

        // then
        assertNotNull(result);
        log.info("EXPENSE 타입 + APPROVED 상태 결재 요청 개수: {}", result.getTotalElements());
        result.getContent().forEach(request -> {
            assertEquals("EXPENSE", request.getRequestType());
            assertEquals(ApprovalStatus.APPROVED, request.getStatusSnapshot());
            log.info("결재 요청: {}", request);
        });
    }

    @Test
    @Transactional
    public void testFindByRequestTypeAndRefId() {
        // given
        Member requester = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        // 먼저 Expense 생성
        Expense expense = Expense.builder()
                .writer(requester)
                .status(ApprovalStatus.SUBMITTED)
                .merchant("결재 요청 테스트")
                .amount(20000)
                .category("식비")
                .receiptDate(java.time.LocalDate.now())
                .build();
        Expense savedExpense = expenseRepository.save(expense);

        // ApprovalRequest 생성
        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .requestType("EXPENSE")
                .refId(savedExpense.getId())
                .requester(requester)
                .statusSnapshot(ApprovalStatus.SUBMITTED)
                .build();
        approvalRequestRepository.save(approvalRequest);

        // when
        Optional<ApprovalRequest> result = approvalRequestRepository.findByRequestTypeAndRefId(
                "EXPENSE",
                savedExpense.getId()
        );

        // then
        assertTrue(result.isPresent());
        assertEquals("EXPENSE", result.get().getRequestType());
        assertEquals(savedExpense.getId(), result.get().getRefId());
        log.info("조회된 결재 요청: {}", result.get());
    }

    @Test
    @Transactional
    public void testFindByApproverId() {
        // given
        Member approver = memberRepository.findByEmployeeNo("ADMIN001")
                .orElseGet(() -> {
                    // ADMIN001이 없으면 USER001 사용
                    return memberRepository.findByEmployeeNo("USER001")
                            .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));
                });

        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<ApprovalRequest> result = approvalRequestRepository.findByApproverId(
                approver.getId(),
                pageable
        );

        // then
        assertNotNull(result);
        log.info("결재자별 결재 요청 개수: {}", result.getTotalElements());
        result.getContent().forEach(request -> {
            if (request.getApprover() != null) {
                assertEquals(approver.getId(), request.getApprover().getId());
            }
            log.info("결재 요청: {}", request);
        });
    }
}

