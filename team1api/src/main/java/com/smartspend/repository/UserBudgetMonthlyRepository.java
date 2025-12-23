package com.smartspend.repository;

import com.smartspend.domain.UserBudgetMonthly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBudgetMonthlyRepository extends JpaRepository<UserBudgetMonthly, Long> {

    Optional<UserBudgetMonthly> findByUserIdAndYearMonth(Long userId, String yearMonth);

    @Query(value = 
        "SELECT u.id, u.name, u.department_name, " +
        "       ubm.monthly_limit, " +
        "       COALESCE(SUM(e.amount), 0) as totalExpense, " +
        "       (ubm.monthly_limit - COALESCE(SUM(e.amount), 0)) as remaining " +
        "FROM user u " +
        "JOIN user_budget_monthly ubm ON u.id = ubm.user_id " +
        "LEFT JOIN expense e ON u.id = e.user_id " +
        "  AND e.approval_status = 'APPROVED' " +
        "  AND YEAR(e.receipt_date) = YEAR(STR_TO_DATE(CONCAT(ubm.year_month, '-01'), '%Y-%m-%d')) " +
        "  AND MONTH(e.receipt_date) = MONTH(STR_TO_DATE(CONCAT(ubm.year_month, '-01'), '%Y-%m-%d')) " +
        "WHERE ubm.year_month = :yearMonth " +
        "  AND u.role = 'USER' " +
        "GROUP BY u.id, u.name, u.department_name, ubm.monthly_limit " +
        "HAVING (COALESCE(SUM(e.amount), 0) / ubm.monthly_limit * 100) >= 80 " +
        "ORDER BY (COALESCE(SUM(e.amount), 0) / ubm.monthly_limit * 100) DESC",
        nativeQuery = true)
    List<Object[]> findOverBudgetUsers(@Param("yearMonth") String yearMonth);
}

