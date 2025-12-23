package com.smartspend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "approval_request", indexes = {
    @Index(name = "idx_request_type_ref", columnList = "request_type, ref_id"),
    @Index(name = "idx_approver_status", columnList = "approver_id, status_snapshot"),
    @Index(name = "idx_status_updated", columnList = "status_snapshot, updated_at")
})
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"requester", "approver"})
public class ApprovalRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_type", length = 30, nullable = false)
    private String requestType; // EXPENSE 또는 STORE_ORDER

    @Column(name = "ref_id", nullable = false)
    private Long refId; // expense.id 또는 store_order.id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Member approver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_snapshot", length = 30, nullable = false)
    private ApprovalStatus statusSnapshot;

    // 도메인 메서드
    public void syncStatusSnapshot(ApprovalStatus status) {
        this.statusSnapshot = status;
    }
}

