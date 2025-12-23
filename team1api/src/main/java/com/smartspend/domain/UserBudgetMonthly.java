package com.smartspend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "user_budget_monthly", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "year_month"})
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBudgetMonthly extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    @Column(name = "year_month", length = 7, nullable = false)
    private String yearMonth; // YYYY-MM 형식

    @Column(name = "monthly_limit", nullable = false)
    private Integer monthlyLimit;

    @Column(name = "note", length = 255)
    private String note;

    // 도메인 메서드
    public LocalDate getYearMonthAsDate() {
        return LocalDate.parse(yearMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}

