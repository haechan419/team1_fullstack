package com.smartspend.service;

import com.smartspend.dto.DepartmentStatisticsDTO;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.MemberRepository;
import com.smartspend.repository.UserBudgetMonthlyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountingService 테스트")
class AccountingServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserBudgetMonthlyRepository userBudgetMonthlyRepository;

    @InjectMocks
    private AccountingServiceImpl accountingService;

    @BeforeEach
    void setUp() {
        // 공통 설정
    }

    @Test
    @DisplayName("부서별 통계 조회 성공")
    void testGetDepartmentStatistics() {
        // given
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{"개발팀", 10L, 500000L});
        results.add(new Object[]{"영업팀", 5L, 300000L});

        when(expenseRepository.findDepartmentStatistics("APPROVED"))
                .thenReturn(results);

        // when
        List<DepartmentStatisticsDTO> result = accountingService.getDepartmentStatistics("APPROVED");

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("개발팀", result.get(0).getDepartmentName());
        assertEquals(10L, result.get(0).getExpenseCount());
        assertEquals(500000L, result.get(0).getTotalAmount());
    }

    @Test
    @DisplayName("부서 목록 조회 성공")
    void testGetDepartments() {
        // given
        List<String> departments = new ArrayList<>();
        departments.add("개발팀");
        departments.add("영업팀");
        departments.add("마케팅팀");

        when(memberRepository.findDistinctDepartmentNames())
                .thenReturn(departments);

        // when
        List<String> result = accountingService.getDepartments();

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("개발팀"));
        assertTrue(result.contains("영업팀"));
        assertTrue(result.contains("마케팅팀"));
    }

    @Test
    @DisplayName("카테고리별 통계 조회 성공")
    void testGetCategoryStatistics() {
        // given
        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{"식비", 20L, 400000L});
        results.add(new Object[]{"비품", 15L, 600000L});

        when(expenseRepository.findCategoryStatistics("APPROVED"))
                .thenReturn(results);

        // when
        List<Map<String, Object>> result = accountingService.getCategoryStatistics("APPROVED");

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("식비", result.get(0).get("name"));
        assertEquals(400000L, result.get(0).get("amount"));
    }

    @Test
    @DisplayName("전체 통계 요약 조회 성공")
    void testGetSummary() {
        // given
        when(expenseRepository.countTodayPending()).thenReturn(5L);
        when(expenseRepository.sumMonthlyTotalExpense("APPROVED")).thenReturn(1000000L);
        when(userBudgetMonthlyRepository.findAll()).thenReturn(new ArrayList<>());

        // when
        Map<String, Object> result = accountingService.getSummary();

        // then
        assertNotNull(result);
        assertTrue(result.containsKey("totalBudgetExecutionRate"));
        assertTrue(result.containsKey("todayPendingCount"));
        assertTrue(result.containsKey("monthlyTotalExpense"));
        assertTrue(result.containsKey("overBudgetCount"));
        assertEquals(5L, result.get("todayPendingCount"));
        assertEquals(1000000L, result.get("monthlyTotalExpense"));
    }

    @Test
    @DisplayName("예산 초과 인원 리스트 조회 성공")
    void testGetOverBudgetList() {
        // given
        List<Object[]> results = new ArrayList<>();
        // findOverBudgetUsers 쿼리 결과 순서: id, name, department_name, monthly_limit, totalExpense, remaining
        results.add(new Object[]{1L, "홍길동", "개발팀", 2000000L, 1800000L, 200000L});

        when(userBudgetMonthlyRepository.findOverBudgetUsers(anyString()))
                .thenReturn(results);

        // when
        List<Map<String, Object>> result = accountingService.getOverBudgetList();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("홍길동", result.get(0).get("name"));
        assertEquals("개발팀", result.get(0).get("department"));
        // 실행률 계산: 1800000 / 2000000 * 100 = 90.0, Math.round(90.0) = 90
        assertEquals(90L, ((Number) result.get(0).get("executionRate")).longValue());
        assertEquals(200000L, ((Number) result.get(0).get("remaining")).longValue());
    }
}

