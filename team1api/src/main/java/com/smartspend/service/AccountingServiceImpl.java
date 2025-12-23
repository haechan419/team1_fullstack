package com.smartspend.service;

import com.smartspend.domain.ApprovalStatus;
import com.smartspend.dto.DepartmentStatisticsDTO;
import com.smartspend.repository.ApprovalRequestRepository;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.MemberRepository;
import com.smartspend.repository.UserBudgetMonthlyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountingServiceImpl implements AccountingService {

    private final ExpenseRepository expenseRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final MemberRepository memberRepository;
    private final UserBudgetMonthlyRepository userBudgetMonthlyRepository;

    @Override
    public List<DepartmentStatisticsDTO> getDepartmentStatistics(String status) {
        // status가 null이면 APPROVED로 기본값 설정
        String statusValue = (status != null && !status.isEmpty()) 
            ? status 
            : ApprovalStatus.APPROVED.name();

        log.info("🔍 부서별 통계 조회 - status: {}", statusValue);

        List<Object[]> results = expenseRepository.findDepartmentStatistics(statusValue);

        List<DepartmentStatisticsDTO> dtoList = results.stream()
            .map(row -> DepartmentStatisticsDTO.builder()
                .departmentName((String) row[0])
                .expenseCount(((Number) row[1]).longValue())
                .totalAmount(((Number) row[2]).longValue())
                .build())
            .collect(Collectors.toList());

        log.info("✅ 부서별 통계 조회 결과 - 총 {}개 부서", dtoList.size());
        return dtoList;
    }

    @Override
    public List<String> getDepartments() {
        log.info("🔍 부서 목록 조회");
        List<String> departments = memberRepository.findDistinctDepartmentNames();
        log.info("✅ 부서 목록 조회 결과 - 총 {}개 부서", departments.size());
        return departments;
    }

    @Override
    // 카테고리별 통계 조회
    public List<Map<String, Object>> getCategoryStatistics(String status) {
        String statusValue = (status != null && !status.isEmpty()) 
            ? status 
            : ApprovalStatus.APPROVED.name();

        log.info("🔍 카테고리별 통계 조회 - status: {}", statusValue);

        List<Object[]> results = expenseRepository.findCategoryStatistics(statusValue);

        List<Map<String, Object>> dtoList = results.stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", row[0] != null ? (String) row[0] : "기타");
                map.put("amount", ((Number) row[2]).longValue());
                return map;
            })
            .collect(Collectors.toList());

        log.info("✅ 카테고리별 통계 조회 결과 - 총 {}개 카테고리", dtoList.size());
        return dtoList;
    }

    @Override
    // 전체 통계 요약 조회
    public Map<String, Object> getSummary() {
        log.info("🔍 전체 통계 요약 조회");

        // ApprovalRequest 테이블에서 오늘의 미결재 건수 조회 (더 정확함)
        Long todayPendingCount = approvalRequestRepository.countTodayPending();
        Long monthlyTotalExpense = expenseRepository.sumMonthlyTotalExpense(ApprovalStatus.APPROVED.name());

        // 현재 월의 yearMonth 형식 (YYYY-MM)
        String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Object[]> overBudgetUsers = userBudgetMonthlyRepository.findOverBudgetUsers(currentYearMonth);

        // 예산 집행률 계산: 전체 사용자의 월간 예산 대비 실제 지출 비율
        double totalBudgetExecutionRate = 0.0;
        try {
            // 현재 월의 전체 예산 합계 (더 효율적인 방법)
            Long totalBudget = userBudgetMonthlyRepository.findAll().stream()
                .filter(ubm -> currentYearMonth.equals(ubm.getYearMonth()))
                .mapToLong(ubm -> ubm.getMonthlyLimit())
                .sum();
            
            // 현재 월의 전체 지출 합계 (APPROVED 상태만)
            Long totalExpense = monthlyTotalExpense != null ? monthlyTotalExpense : 0L;
            
            if (totalBudget > 0 && totalBudget > 0) {
                totalBudgetExecutionRate = (totalExpense.doubleValue() / totalBudget.doubleValue()) * 100.0;
            } else if (totalBudget == 0) {
                log.warn("⚠️ 현재 월({})의 예산 데이터가 없습니다.", currentYearMonth);
            }
            
            log.info("📊 예산 집행률 계산 - 총 예산: {}, 총 지출: {}, 집행률: {}%", 
                totalBudget, totalExpense, String.format("%.2f", totalBudgetExecutionRate));
        } catch (Exception e) {
            log.warn("⚠️ 예산 집행률 계산 실패: {}", e.getMessage(), e);
            totalBudgetExecutionRate = 0.0;
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBudgetExecutionRate", Math.round(totalBudgetExecutionRate * 100.0) / 100.0); // 소수점 2자리
        summary.put("todayPendingCount", todayPendingCount != null ? todayPendingCount : 0L);
        summary.put("monthlyTotalExpense", monthlyTotalExpense != null ? monthlyTotalExpense : 0L);
        summary.put("overBudgetCount", overBudgetUsers.size());

        log.info("✅ 전체 통계 요약 조회 결과 - 집행률: {}%, 미결재: {}건, 월간 지출: {}원, 예산 초과: {}명", 
            String.format("%.2f", totalBudgetExecutionRate), 
            summary.get("todayPendingCount"),
            summary.get("monthlyTotalExpense"),
            summary.get("overBudgetCount"));
        return summary;
    }

    @Override
    // 예산 초과 인원 리스트 조회
    public List<Map<String, Object>> getOverBudgetList() {
        log.info("🔍 예산 초과 인원 리스트 조회");

        String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Object[]> results = userBudgetMonthlyRepository.findOverBudgetUsers(currentYearMonth);

        List<Map<String, Object>> dtoList = results.stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", row[1] != null ? (String) row[1] : "");
                map.put("department", row[2] != null ? (String) row[2] : "");
                
                Long monthlyLimit = ((Number) row[3]).longValue();
                Long totalExpense = ((Number) row[4]).longValue();
                Long remaining = ((Number) row[5]).longValue();
                
                double executionRate = monthlyLimit > 0 
                    ? (totalExpense.doubleValue() / monthlyLimit.doubleValue() * 100) 
                    : 0.0;
                
                map.put("executionRate", Math.round(executionRate));
                map.put("remaining", remaining);
                return map;
            })
            .collect(Collectors.toList());

        log.info("✅ 예산 초과 인원 리스트 조회 결과 - 총 {}명", dtoList.size());
        return dtoList;
    }
}

