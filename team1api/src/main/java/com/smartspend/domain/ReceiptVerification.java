package com.smartspend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receipt_verification", indexes = {
    @Index(name = "idx_verified_by_created", columnList = "verified_by, created_at")
})
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"expense", "verifiedBy"})
public class ReceiptVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false, unique = true)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by", nullable = false)
    private Member verifiedBy;

    @Column(name = "verified_merchant", length = 150)
    private String verifiedMerchant;

    @Column(name = "verified_amount")
    private Integer verifiedAmount;

    @Column(name = "verified_category", length = 50)
    private String verifiedCategory;

    @Column(name = "reason", length = 255)
    private String reason;
}

