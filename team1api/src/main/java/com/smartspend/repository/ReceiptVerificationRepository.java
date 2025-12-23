package com.smartspend.repository;

import com.smartspend.domain.ReceiptVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptVerificationRepository extends JpaRepository<ReceiptVerification, Long> {

    // 지출 ID로 검증 결과 조회
    Optional<ReceiptVerification> findByExpenseId(Long expenseId);
}

