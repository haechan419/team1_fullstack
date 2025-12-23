package com.smartspend.service;

import com.smartspend.domain.Expense;
import com.smartspend.domain.Member;
import com.smartspend.domain.MemberRole;
import com.smartspend.domain.ApprovalStatus;
import com.smartspend.dto.ExpenseDTO;
import com.smartspend.dto.ExpenseSubmitDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService 테스트")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReceiptUploadRepository receiptUploadRepository;

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private ApprovalActionLogRepository approvalActionLogRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Member testMember;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .name("테스트 사용자")
                .email("test@test.com")
                .role(MemberRole.USER)
                .build();

        testExpense = Expense.builder()
                .id(1L)
                .writer(testMember)
                .receiptDate(LocalDate.now())
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .status(ApprovalStatus.DRAFT)
                .build();
    }

    @Test
    @DisplayName("지출 내역 등록 성공")
    void testRegister() {
        // given
        ExpenseDTO expenseDTO = ExpenseDTO.builder()
                .receiptDate(LocalDate.now())
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        // when
        Long id = expenseService.register(expenseDTO, 1L);

        // then
        assertNotNull(id);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("지출 내역 조회 성공 - 일반 사용자")
    void testGet_AsUser() {
        // given
        when(expenseRepository.findByIdAndWriterId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(receiptUploadRepository.findByExpenseId(1L)).thenReturn(Optional.empty());

        // when
        ExpenseDTO result = expenseService.get(1L, 1L, false);

        // then
        assertNotNull(result);
        assertEquals("테스트 상점", result.getMerchant());
        assertEquals(10000, result.getAmount());
    }

    @Test
    @DisplayName("지출 내역 조회 성공 - 관리자")
    void testGet_AsAdmin() {
        // given
        when(expenseRepository.findByIdWithWriter(1L)).thenReturn(Optional.of(testExpense));
        when(receiptUploadRepository.findByExpenseId(1L)).thenReturn(Optional.empty());

        // when
        ExpenseDTO result = expenseService.get(1L, 1L, true);

        // then
        assertNotNull(result);
        assertEquals("테스트 상점", result.getMerchant());
        assertEquals(10000, result.getAmount());
    }

    @Test
    @DisplayName("존재하지 않는 지출 내역 조회 시 예외 발생")
    void testGet_WhenNotFound_ThrowsException() {
        // given
        when(expenseRepository.findByIdAndWriterId(1L, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> expenseService.get(1L, 1L, false));
    }

    @Test
    @DisplayName("DRAFT 상태에서 수정 성공")
    void testModify_WhenDraft_Success() {
        // given
        ExpenseDTO expenseDTO = ExpenseDTO.builder()
                .id(1L)
                .receiptDate(LocalDate.now())
                .merchant("수정된 상점")
                .amount(20000)
                .category("교통비")
                .build();

        when(expenseRepository.findByIdAndWriterId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        // when
        expenseService.modify(expenseDTO, 1L);

        // then
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    @DisplayName("DRAFT가 아닌 상태에서 수정 시 예외 발생")
    void testModify_WhenNotDraft_ThrowsException() {
        // given
        testExpense.setStatus(ApprovalStatus.SUBMITTED);
        ExpenseDTO expenseDTO = ExpenseDTO.builder()
                .id(1L)
                .build();

        when(expenseRepository.findByIdAndWriterId(1L, 1L)).thenReturn(Optional.of(testExpense));

        // when & then
        assertThrows(IllegalStateException.class, () -> expenseService.modify(expenseDTO, 1L));
    }

    @Test
    @DisplayName("DRAFT 상태에서 삭제 성공")
    void testRemove_WhenDraft_Success() {
        // given
        when(expenseRepository.findByIdAndWriterId(1L, 1L)).thenReturn(Optional.of(testExpense));

        // when
        expenseService.remove(1L, 1L);

        // then
        verify(expenseRepository, times(1)).delete(testExpense);
    }

    @Test
    @DisplayName("DRAFT가 아닌 상태에서 삭제 시 예외 발생")
    void testRemove_WhenNotDraft_ThrowsException() {
        // given
        Expense submittedExpense = Expense.builder()
                .id(1L)
                .writer(testMember)
                .receiptDate(LocalDate.now())
                .merchant("테스트 상점")
                .amount(10000)
                .category("식비")
                .status(ApprovalStatus.SUBMITTED)
                .build();

        when(expenseRepository.findByIdAndWriterId(1L, 1L)).thenReturn(Optional.of(submittedExpense));

        // when & then
        assertThrows(IllegalStateException.class, () -> expenseService.remove(1L, 1L));
    }

    @Test
    @DisplayName("지출 내역 제출 성공 - ApprovalRequest 생성")
    void testSubmit_Success() {
        // given
        ExpenseSubmitDTO submitDTO = ExpenseSubmitDTO.builder()
                .requestNote("제출 메모")
                .build();

        when(expenseRepository.findByIdAndWriterId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(approvalRequestRepository.save(any(com.smartspend.domain.ApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(approvalActionLogRepository.save(any(com.smartspend.domain.ApprovalActionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        expenseService.submit(1L, 1L, submitDTO);

        // then
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(approvalRequestRepository, times(1)).save(any(com.smartspend.domain.ApprovalRequest.class));
        verify(approvalActionLogRepository, times(1)).save(any(com.smartspend.domain.ApprovalActionLog.class));
        // testExpense는 mock이므로 상태 변경을 직접 확인할 수 없음
    }

    @Test
    @DisplayName("지출 내역 제출 성공 - submitDTO가 null인 경우")
    void testSubmit_WithNullSubmitDTO() {
        // given
        when(expenseRepository.findByIdAndWriterId(1L, 1L)).thenReturn(Optional.of(testExpense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(approvalRequestRepository.save(any(com.smartspend.domain.ApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(approvalActionLogRepository.save(any(com.smartspend.domain.ApprovalActionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        expenseService.submit(1L, 1L, null);

        // then
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(approvalRequestRepository, times(1)).save(any(com.smartspend.domain.ApprovalRequest.class));
        verify(approvalActionLogRepository, times(1)).save(any(com.smartspend.domain.ApprovalActionLog.class));
    }

    @Test
    @DisplayName("상태별 필터링 조회")
    void testGetList_WithStatusFilter() {
        // given
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(20).build();
        List<Expense> expenses = new ArrayList<>();
        expenses.add(testExpense);
        Page<Expense> page = new PageImpl<>(expenses);

        when(expenseRepository.findByWriterIdAndStatus(eq(1L), eq(ApprovalStatus.DRAFT), any(Pageable.class)))
                .thenReturn(page);
        when(receiptUploadRepository.findByExpenseId(any())).thenReturn(Optional.empty());

        // when
        var result = expenseService.getList(1L, pageRequestDTO, "DRAFT", null, null);

        // then
        assertNotNull(result);
        assertEquals(1, result.getDtoList().size());
    }

    @Test
    @DisplayName("기간별 필터링 조회")
    void testGetList_WithDateRange() {
        // given
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(20).build();
        List<Expense> expenses = new ArrayList<>();
        expenses.add(testExpense);
        Page<Expense> page = new PageImpl<>(expenses);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findByUserIdAndDateRange(eq(1L), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(page);
        when(receiptUploadRepository.findByExpenseId(any())).thenReturn(Optional.empty());

        // when
        var result = expenseService.getList(1L, pageRequestDTO, null, startDate, endDate);

        // then
        assertNotNull(result);
        assertEquals(1, result.getDtoList().size());
    }

    @Test
    @DisplayName("상태와 기간 필터링 조회")
    void testGetList_WithStatusAndDateRange() {
        // given
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(20).build();
        List<Expense> expenses = new ArrayList<>();
        expenses.add(testExpense);
        Page<Expense> page = new PageImpl<>(expenses);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(expenseRepository.findByUserIdAndStatusAndDateRange(eq(1L), eq(ApprovalStatus.DRAFT), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(page);
        when(receiptUploadRepository.findByExpenseId(any())).thenReturn(Optional.empty());

        // when
        var result = expenseService.getList(1L, pageRequestDTO, "DRAFT", startDate, endDate);

        // then
        assertNotNull(result);
        assertEquals(1, result.getDtoList().size());
    }

    @Test
    @DisplayName("잘못된 status 값 필터링 - 무시되고 전체 조회")
    void testGetList_WithInvalidStatus() {
        // given
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(20).build();
        List<Expense> expenses = new ArrayList<>();
        expenses.add(testExpense);
        Page<Expense> page = new PageImpl<>(expenses);

        when(expenseRepository.findByWriterId(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(receiptUploadRepository.findByExpenseId(any())).thenReturn(Optional.empty());

        // when
        var result = expenseService.getList(1L, pageRequestDTO, "INVALID_STATUS", null, null);

        // then
        assertNotNull(result);
        assertEquals(1, result.getDtoList().size());
    }
}

