package com.smartspend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "expense", indexes = {
    @Index(name = "idx_user_receipt_date", columnList = "user_id, receipt_date"),
    @Index(name = "idx_approval_status_updated", columnList = "approval_status, updated_at")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"writer"})
public class Expense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member writer;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 30, nullable = false)
    private ApprovalStatus status;

    @Column(name = "merchant", length = 150)
    private String merchant;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "receipt_image_url", length = 255)
    private String receiptImageUrl;

    @Column(name = "description", length = 255)
    private String description;

    // 도메인 메서드
    public void submit() {
        if (this.status != ApprovalStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태의 지출 내역만 제출할 수 있습니다.");
        }
        this.status = ApprovalStatus.SUBMITTED;
    }

    public void approve() {
        this.status = ApprovalStatus.APPROVED;
    }

    public void reject(String adminNote) {
        this.status = ApprovalStatus.REJECTED;
        // adminNote는 별도 필드가 없으므로 description에 저장하거나 별도 처리 필요
    }

    public void requestMoreInfo(String adminNote) {
        this.status = ApprovalStatus.REQUEST_MORE_INFO;
        // adminNote는 별도 필드가 없으므로 description에 저장하거나 별도 처리 필요
    }

    public boolean isDraft() {
        return this.status == ApprovalStatus.DRAFT;
    }

    public boolean canModify() {
        return isDraft();
    }

    public boolean canDelete() {
        return isDraft();
    }
}

