package com.smartspend.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Expense 도메인 테스트")
class ExpenseTest {

    private Expense expense;

    @BeforeEach
    void setUp() {
        expense = Expense.builder()
                .status(ApprovalStatus.DRAFT)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("DRAFT 상태에서 제출하면 SUBMITTED로 변경")
    void testSubmit() {
        // when
        expense.submit();

        // then
        assertEquals(ApprovalStatus.SUBMITTED, expense.getStatus());
    }

    @Test
    @DisplayName("DRAFT가 아닌 상태에서 제출하면 예외 발생")
    void testSubmit_WhenNotDraft_ThrowsException() {
        // given
        expense = Expense.builder()
                .status(ApprovalStatus.SUBMITTED)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();

        // when & then
        assertThrows(IllegalStateException.class, () -> expense.submit());
    }

    @Test
    @DisplayName("승인하면 APPROVED로 변경")
    void testApprove() {
        // when
        expense.approve();

        // then
        assertEquals(ApprovalStatus.APPROVED, expense.getStatus());
    }

    @Test
    @DisplayName("반려하면 REJECTED로 변경")
    void testReject() {
        // given
        String reason = "반려 사유";

        // when
        expense.reject(reason);

        // then
        assertEquals(ApprovalStatus.REJECTED, expense.getStatus());
        // adminNote는 별도 필드가 없으므로 description에 저장되지 않음
    }

    @Test
    @DisplayName("보완요청하면 REQUEST_MORE_INFO로 변경")
    void testRequestMoreInfo() {
        // given
        String reason = "보완 요청 사유";

        // when
        expense.requestMoreInfo(reason);

        // then
        assertEquals(ApprovalStatus.REQUEST_MORE_INFO, expense.getStatus());
        // adminNote는 별도 필드가 없으므로 description에 저장되지 않음
    }

    @Test
    @DisplayName("DRAFT 상태 확인")
    void testIsDraft() {
        // given
        expense = Expense.builder()
                .status(ApprovalStatus.DRAFT)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();

        // when & then
        assertTrue(expense.isDraft());
    }

    @Test
    @DisplayName("DRAFT 상태가 아니면 isDraft는 false")
    void testIsDraft_WhenNotDraft_ReturnsFalse() {
        // given
        expense = Expense.builder()
                .status(ApprovalStatus.SUBMITTED)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();

        // when & then
        assertFalse(expense.isDraft());
    }

    @Test
    @DisplayName("DRAFT 상태에서만 수정 가능")
    void testCanModify() {
        // given
        expense = Expense.builder()
                .status(ApprovalStatus.DRAFT)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();

        // when & then
        assertTrue(expense.canModify());

        // given
        expense = Expense.builder()
                .status(ApprovalStatus.SUBMITTED)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();

        // when & then
        assertFalse(expense.canModify());
    }

    @Test
    @DisplayName("DRAFT 상태에서만 삭제 가능")
    void testCanDelete() {
        // given
        expense = Expense.builder()
                .status(ApprovalStatus.DRAFT)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();

        // when & then
        assertTrue(expense.canDelete());

        // given
        expense = Expense.builder()
                .status(ApprovalStatus.SUBMITTED)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();

        // when & then
        assertFalse(expense.canDelete());
    }
}

