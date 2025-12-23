package com.smartspend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "receipt_upload", indexes = {
    @Index(name = "idx_expense_id", columnList = "expense_id"),
    @Index(name = "idx_file_hash", columnList = "file_hash")
})
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"expense", "uploadedBy"})
public class ReceiptUpload extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false, unique = true)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Member uploadedBy;

    @Column(name = "file_url", length = 255, nullable = false)
    private String fileUrl;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "mime_type", length = 50)
    private String mimeType;
}

