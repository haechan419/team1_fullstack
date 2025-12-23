package com.smartspend.service;

import com.smartspend.domain.ApprovalActionLog;
import com.smartspend.domain.ApprovalRequest;
import com.smartspend.domain.ApprovalStatus;
import com.smartspend.domain.Expense;
import com.smartspend.domain.Member;
import com.smartspend.domain.MemberRole;
import com.smartspend.dto.ApprovalActionDTO;
import com.smartspend.dto.ApprovalRequestDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalService 테스트")
class ApprovalServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private ApprovalActionLogRepository approvalActionLogRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private Member adminMember;
    private Member regularMember;
    private Expense testExpense;
    private ApprovalRequest testApprovalRequest;

    @BeforeEach
    void setUp() {
        adminMember = Member.builder()
                .id(1L)
                .name("관리자")
                .email("admin@test.com")
                .role(MemberRole.ADMIN)
                .build();

        regularMember = Member.builder()
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

        testApprovalRequest = ApprovalRequest.builder()
                .id(1L)
                .requestType("EXPENSE")
                .refId(1L)
                .requester(regularMember)
                .approver(adminMember)
                .statusSnapshot(ApprovalStatus.SUBMITTED)
                .build();
        // BaseEntity 필드는 builder에 포함되지 않으므로 reflection 사용
        try {
            Field createdAtField = testApprovalRequest.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testApprovalRequest, LocalDateTime.now());
            
            Field updatedAtField = testApprovalRequest.getClass().getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(testApprovalRequest, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set BaseEntity fields", e);
        }
    }

    @Test
    @DisplayName("결재 요청 목록 조회 성공 - 일반 사용자")
    void testGetList_AsUser() {
        // given
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(20).build();
        List<ApprovalRequest> requests = new ArrayList<>();
        requests.add(testApprovalRequest);
        Page<ApprovalRequest> page = new PageImpl<>(requests);

        // 일반 사용자는 findByRequesterIdOrderByReceiptDate 호출 (status가 null인 경우)
        when(approvalRequestRepository.findByRequesterIdOrderByReceiptDate(eq(2L), any(Pageable.class)))
                .thenReturn(page);

        // when
        PageResponseDTO<ApprovalRequestDTO> result = approvalService.getList(2L, false, pageRequestDTO, null, null, null, null);

        // then
        assertNotNull(result);
        assertEquals(1, result.getDtoList().size());
        ApprovalRequestDTO dto = result.getDtoList().get(0);
        assertEquals(1L, dto.getId());
        assertEquals("EXPENSE", dto.getRequestType());
        assertEquals(2L, dto.getRequesterId());
        assertEquals("일반 사용자", dto.getRequesterName());
        assertEquals(1L, dto.getApproverId());
        assertEquals("관리자", dto.getApproverName());
    }

    @Test
    @DisplayName("결재 요청 목록 조회 성공 - 관리자")
    void testGetList_AsAdmin() {
        // given
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(20).build();
        List<ApprovalRequest> requests = new ArrayList<>();
        requests.add(testApprovalRequest);
        Page<ApprovalRequest> page = new PageImpl<>(requests);

        // 관리자는 findByRequestTypeOrderByReceiptDate 호출 (상신일 필터가 없는 경우)
        when(approvalRequestRepository.findByRequestTypeOrderByReceiptDate(eq("EXPENSE"), any(Pageable.class)))
                .thenReturn(page);

        // when
        PageResponseDTO<ApprovalRequestDTO> result = approvalService.getList(1L, true, pageRequestDTO, "EXPENSE", null, null, null);

        // then
        assertNotNull(result);
        assertEquals(1, result.getDtoList().size());
        ApprovalRequestDTO dto = result.getDtoList().get(0);
        assertEquals(1L, dto.getId());
        assertEquals("EXPENSE", dto.getRequestType());
        assertEquals("SUBMITTED", dto.getStatusSnapshot());
    }

    @Test
    @DisplayName("결재 승인 성공")
    void testAction_Approve_Success() {
        // given
        ApprovalActionDTO actionDTO = ApprovalActionDTO.builder()
                .action("APPROVE")
                .message("승인합니다.")
                .build();

        when(approvalRequestRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testApprovalRequest));
        when(expenseRepository.findByIdWithWriter(1L)).thenReturn(Optional.of(testExpense));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(adminMember));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);
        when(approvalActionLogRepository.save(any(ApprovalActionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        approvalService.action(1L, actionDTO, 1L);

        // then
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
        verify(approvalActionLogRepository, times(1)).save(any(ApprovalActionLog.class));
    }

    @Test
    @DisplayName("결재 반려 성공")
    void testAction_Reject_Success() {
        // given
        ApprovalActionDTO actionDTO = ApprovalActionDTO.builder()
                .action("REJECT")
                .message("반려 사유: 불필요한 지출")
                .build();

        when(approvalRequestRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testApprovalRequest));
        when(expenseRepository.findByIdWithWriter(1L)).thenReturn(Optional.of(testExpense));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(adminMember));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);
        when(approvalActionLogRepository.save(any(ApprovalActionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        approvalService.action(1L, actionDTO, 1L);

        // then
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
        verify(approvalActionLogRepository, times(1)).save(any(ApprovalActionLog.class));
    }

    @Test
    @DisplayName("보완 요청 성공")
    void testAction_RequestMoreInfo_Success() {
        // given
        ApprovalActionDTO actionDTO = ApprovalActionDTO.builder()
                .action("REQUEST_MORE_INFO")
                .message("추가 자료가 필요합니다.")
                .build();

        when(approvalRequestRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(testApprovalRequest));
        when(expenseRepository.findByIdWithWriter(1L)).thenReturn(Optional.of(testExpense));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(adminMember));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(approvalRequestRepository.save(any(ApprovalRequest.class))).thenReturn(testApprovalRequest);
        when(approvalActionLogRepository.save(any(ApprovalActionLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        approvalService.action(1L, actionDTO, 1L);

        // then
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(approvalRequestRepository, times(1)).save(any(ApprovalRequest.class));
        verify(approvalActionLogRepository, times(1)).save(any(ApprovalActionLog.class));
    }

    @Test
    @DisplayName("존재하지 않는 결재 요청 조회 시 예외 발생")
    void testGet_WhenNotFound_ThrowsException() {
        // given
        when(approvalRequestRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> approvalService.get(1L, 2L, false));
    }
}

