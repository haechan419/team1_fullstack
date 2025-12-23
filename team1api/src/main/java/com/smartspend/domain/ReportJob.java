package com.smartspend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "report_job", indexes = {
    @Index(name = "idx_requested_by_created", columnList = "requested_by, created_at"),
    @Index(name = "idx_status_updated", columnList = "status, updated_at")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private Member requestedBy;

    @Column(name = "scope_type", length = 30, nullable = false)
    private String scopeType; // MY, DEPARTMENT_NAME, ALL

    @Column(name = "scope_value", length = 100)
    private String scopeValue; // 부서명 등

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "template", length = 50, nullable = false)
    private String template; // EXCEL, PDF

    @Column(name = "report_type", length = 50, nullable = false)
    private String reportType; // DETAIL_EXCEL, PERSONAL_PDF, DEPT_EXCEL (AI_INSIGHT_PDF는 제외됨)

    @Column(name = "status", length = 30, nullable = false)
    private String status; // QUEUED, RUNNING, DONE, FAILED

    // 도메인 메서드
    public void markAsRunning() {
        this.status = "RUNNING";
    }

    public void markAsDone() {
        this.status = "DONE";
    }

    public void markAsFailed() {
        this.status = "FAILED";
    }
}

