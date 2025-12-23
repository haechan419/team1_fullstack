package com.smartspend.repository;

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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
public class ExpenseRepositoryTests {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    public void testInsert() {
        // given
        Member testMember = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        Expense expense = Expense.builder()
                .writer(testMember)
                .status(ApprovalStatus.DRAFT)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .description("테스트 지출 내역")
                .build();

        // when
        Expense saved = expenseRepository.save(expense);

        // then
        assertNotNull(saved.getId());
        log.info("저장된 지출 내역 ID: {}", saved.getId());
        log.info("저장된 지출 내역: {}", saved);
    }

    @Test
    @Transactional
    public void testFindByWriterId() {
        // given
        Member testMember = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<Expense> result = expenseRepository.findByWriterId(testMember.getId(), pageable);

        // then
        assertNotNull(result);
        log.info("총 개수: {}", result.getTotalElements());
        log.info("페이지 수: {}", result.getTotalPages());
        result.getContent().forEach(expense -> log.info(expense));
    }

    @Test
    @Transactional
    public void testFindByWriterIdAndStatus() {
        // given
        Member testMember = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<Expense> result = expenseRepository.findByWriterIdAndStatus(
                testMember.getId(), 
                ApprovalStatus.APPROVED, 
                pageable
        );

        // then
        assertNotNull(result);
        log.info("APPROVED 상태 지출 내역 개수: {}", result.getTotalElements());
        result.getContent().forEach(expense -> {
            assertEquals(ApprovalStatus.APPROVED, expense.getStatus());
            log.info(expense);
        });
    }

    @Test
    @Transactional
    public void testFindByUserIdAndDateRange() {
        // given
        Member testMember = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("receiptDate").descending());

        // when
        Page<Expense> result = expenseRepository.findByUserIdAndDateRange(
                testMember.getId(),
                startDate,
                endDate,
                pageable
        );

        // then
        assertNotNull(result);
        log.info("기간별 지출 내역 개수: {}", result.getTotalElements());
        result.getContent().forEach(expense -> {
            assertTrue(expense.getReceiptDate().isAfter(startDate.minusDays(1)));
            assertTrue(expense.getReceiptDate().isBefore(endDate.plusDays(1)));
            log.info("지출 일자: {}, 금액: {}", expense.getReceiptDate(), expense.getAmount());
        });
    }

    @Test
    @Transactional
    public void testFindByIdAndWriterId() {
        // given
        Member testMember = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        // 실제 존재하는 지출 ID를 사용하거나, 먼저 생성
        Expense expense = Expense.builder()
                .writer(testMember)
                .status(ApprovalStatus.DRAFT)
                .merchant("권한 테스트 상점")
                .amount(5000)
                .category("교통비")
                .receiptDate(LocalDate.now())
                .build();
        Expense saved = expenseRepository.save(expense);

        // when
        Optional<Expense> result = expenseRepository.findByIdAndWriterId(
                saved.getId(),
                testMember.getId()
        );

        // then
        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
        assertEquals(testMember.getId(), result.get().getWriter().getId());
        log.info("조회된 지출 내역: {}", result.get());
    }

    @Test
    @Transactional
    public void testFindByIdWithWriter() {
        // given
        Member testMember = memberRepository.findByEmployeeNo("USER001")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));

        Expense expense = Expense.builder()
                .writer(testMember)
                .status(ApprovalStatus.DRAFT)
                .merchant("관리자 조회 테스트")
                .amount(15000)
                .category("비품")
                .receiptDate(LocalDate.now())
                .build();
        Expense saved = expenseRepository.save(expense);

        // when
        Optional<Expense> result = expenseRepository.findByIdWithWriter(saved.getId());

        // then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getWriter());
        assertEquals(testMember.getId(), result.get().getWriter().getId());
        log.info("관리자 조회 결과: {}", result.get());
        log.info("작성자 정보: {}", result.get().getWriter());
    }

    @Test
    @Transactional
    public void testFindByStatus() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending());

        // when
        Page<Expense> result = expenseRepository.findByStatus(ApprovalStatus.DRAFT, pageable);

        // then
        assertNotNull(result);
        log.info("DRAFT 상태 지출 내역 개수: {}", result.getTotalElements());
        result.getContent().forEach(expense -> {
            assertEquals(ApprovalStatus.DRAFT, expense.getStatus());
            log.info(expense);
        });
    }

    @Test
    @Transactional
    public void testFindDepartmentStatistics() {
        // when
        List<Object[]> results = expenseRepository.findDepartmentStatistics("APPROVED");

        // then
        assertNotNull(results);
        log.info("부서별 통계 결과 개수: {}", results.size());
        results.forEach(row -> {
            String departmentName = (String) row[0];
            Long expenseCount = ((Number) row[1]).longValue();
            Long totalAmount = ((Number) row[2]).longValue();
            log.info("부서: {}, 건수: {}, 총액: {}", departmentName, expenseCount, totalAmount);
        });
    }

    @Test
    @Transactional
    public void testFindCategoryStatistics() {
        // when
        List<Object[]> results = expenseRepository.findCategoryStatistics("APPROVED");

        // then
        assertNotNull(results);
        log.info("카테고리별 통계 결과 개수: {}", results.size());
        results.forEach(row -> {
            String categoryName = (String) row[0];
            Long expenseCount = ((Number) row[1]).longValue();
            Long totalAmount = ((Number) row[2]).longValue();
            log.info("카테고리: {}, 건수: {}, 총액: {}", categoryName, expenseCount, totalAmount);
        });
    }

    @Test
    @Transactional
    public void testCountTodayPending() {
        // when
        Long count = expenseRepository.countTodayPending();

        // then
        assertNotNull(count);
        assertTrue(count >= 0);
        log.info("오늘의 미결재 건수: {}", count);
    }

    @Test
    @Transactional
    public void testSumMonthlyTotalExpense() {
        // when
        Long total = expenseRepository.sumMonthlyTotalExpense("APPROVED");

        // then
        assertNotNull(total);
        assertTrue(total >= 0);
        log.info("이번 달 총 지출액 (APPROVED): {}", total);
    }

    @Test
    @Transactional
    public void testFindForReportByDateRange() {
        // given
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        // when
        List<Expense> results = expenseRepository.findForReportByDateRange(startDate, endDate);

        // then
        assertNotNull(results);
        log.info("리포트용 지출 내역 개수: {}", results.size());
        results.forEach(expense -> {
            assertEquals(ApprovalStatus.APPROVED, expense.getStatus());
            assertTrue(expense.getReceiptDate().isAfter(startDate.minusDays(1)));
            assertTrue(expense.getReceiptDate().isBefore(endDate.plusDays(1)));
            log.info("리포트 항목: {}", expense);
        });
    }
}

