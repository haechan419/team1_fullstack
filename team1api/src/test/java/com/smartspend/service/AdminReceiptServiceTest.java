package com.smartspend.service;

import com.smartspend.domain.Expense;
import com.smartspend.domain.ReceiptUpload;
import com.smartspend.domain.Member;
import com.smartspend.domain.MemberRole;
import com.smartspend.domain.ApprovalStatus;
import com.smartspend.dto.ReceiptVerificationDTO;
import com.smartspend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminReceiptService 테스트")
class AdminReceiptServiceTest {

    @Mock
    private ReceiptUploadRepository receiptUploadRepository;

    @Mock
    private ReceiptAiExtractionRepository receiptAiExtractionRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ReceiptVerificationRepository receiptVerificationRepository;

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private ApprovalActionLogRepository approvalActionLogRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private com.smartspend.util.CustomFileUtil customFileUtil;

    @InjectMocks
    private AdminReceiptServiceImpl adminReceiptService;

    private Member adminMember;
    private Expense testExpense;
    private ReceiptUpload testReceipt;

    @BeforeEach
    void setUp() {
        adminMember = Member.builder()
                .id(1L)
                .name("관리자")
                .email("admin@test.com")
                .role(MemberRole.ADMIN)
                .build();

        Member regularMember = Member.builder()
                .id(2L)
                .name("일반 사용자")
                .email("user@test.com")
                .role(MemberRole.USER)
                .build();

        testExpense = Expense.builder()
                .id(1L)
                .writer(regularMember)
                .status(ApprovalStatus.SUBMITTED)
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .receiptDate(LocalDate.now())
                .build();

        testReceipt = ReceiptUpload.builder()
                .id(1L)
                .expense(testExpense)
                .fileUrl("/uploads/receipts/test.jpg")
                .build();
    }

    @Test
    @DisplayName("영수증 승인 성공 - Expense 상태가 APPROVED로 변경")
    void testVerify_Approve_Success() {
        // given
        ReceiptVerificationDTO dto = ReceiptVerificationDTO.builder()
                .action("APPROVE")
                .verifiedMerchant("검증된 상점")
                .verifiedAmount(10000)
                .verifiedCategory("식비")
                .build();

        when(receiptUploadRepository.findById(1L)).thenReturn(Optional.of(testReceipt));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(adminMember));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(approvalRequestRepository.findByRequestTypeAndRefId("EXPENSE", 1L))
                .thenReturn(Optional.empty());
        when(receiptVerificationRepository.findByExpenseId(1L)).thenReturn(Optional.empty());
        when(receiptVerificationRepository.save(any())).thenReturn(any());

        // when
        adminReceiptService.verify(1L, dto, 1L);

        // then
        verify(expenseRepository, times(1)).save(testExpense);
        // testExpense는 실제 객체이므로 상태 변경 확인 가능
        assertEquals(ApprovalStatus.APPROVED, testExpense.getStatus());
    }

    @Test
    @DisplayName("영수증 반려 성공 - Expense 상태가 REJECTED로 변경")
    void testVerify_Reject_Success() {
        // given
        ReceiptVerificationDTO dto = ReceiptVerificationDTO.builder()
                .action("REJECT")
                .reason("반려 사유")
                .build();

        when(receiptUploadRepository.findById(1L)).thenReturn(Optional.of(testReceipt));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(adminMember));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(approvalRequestRepository.findByRequestTypeAndRefId("EXPENSE", 1L))
                .thenReturn(Optional.empty());
        when(receiptVerificationRepository.findByExpenseId(1L)).thenReturn(Optional.empty());
        when(receiptVerificationRepository.save(any())).thenReturn(any());

        // when
        adminReceiptService.verify(1L, dto, 1L);

        // then
        verify(expenseRepository, times(1)).save(testExpense);
        assertEquals(ApprovalStatus.REJECTED, testExpense.getStatus());
        // adminNote는 별도 필드가 없으므로 확인하지 않음
    }

    @Test
    @DisplayName("보완요청 성공 - Expense 상태가 REQUEST_MORE_INFO로 변경")
    void testVerify_RequestMoreInfo_Success() {
        // given
        ReceiptVerificationDTO dto = ReceiptVerificationDTO.builder()
                .action("REQUEST_MORE_INFO")
                .reason("보완 요청 사유")
                .build();

        when(receiptUploadRepository.findById(1L)).thenReturn(Optional.of(testReceipt));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(adminMember));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(approvalRequestRepository.findByRequestTypeAndRefId("EXPENSE", 1L))
                .thenReturn(Optional.empty());
        when(receiptVerificationRepository.findByExpenseId(1L)).thenReturn(Optional.empty());
        when(receiptVerificationRepository.save(any())).thenReturn(any());

        // when
        adminReceiptService.verify(1L, dto, 1L);

        // then
        verify(expenseRepository, times(1)).save(testExpense);
        assertEquals(ApprovalStatus.REQUEST_MORE_INFO, testExpense.getStatus());
        // adminNote는 별도 필드가 없으므로 확인하지 않음
    }

    @Test
    @DisplayName("관리자가 아닌 사용자가 검증 시도 시 예외 발생")
    void testVerify_WhenNotAdmin_ThrowsException() {
        // given
        Member regularMember = Member.builder()
                .id(2L)
                .role(MemberRole.USER)
                .build();

        ReceiptVerificationDTO dto = ReceiptVerificationDTO.builder()
                .action("APPROVE")
                .build();

        when(receiptUploadRepository.findById(1L)).thenReturn(Optional.of(testReceipt));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(regularMember));

        // when & then
        assertThrows(RuntimeException.class, () -> adminReceiptService.verify(1L, dto, 2L));
    }
}

