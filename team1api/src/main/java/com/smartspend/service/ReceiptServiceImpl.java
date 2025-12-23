package com.smartspend.service;

import com.smartspend.domain.Expense;
import com.smartspend.domain.ReceiptAiExtraction;
import com.smartspend.domain.ReceiptUpload;
import com.smartspend.domain.ReceiptVerification;
import com.smartspend.domain.Member;
import com.smartspend.dto.ReceiptDTO;
import com.smartspend.dto.ReceiptExtractionDTO;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.MemberRepository;
import com.smartspend.repository.ReceiptAiExtractionRepository;
import com.smartspend.repository.ReceiptUploadRepository;
import com.smartspend.repository.ReceiptVerificationRepository;
import com.smartspend.util.CustomFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptUploadRepository receiptUploadRepository;
    private final ReceiptAiExtractionRepository receiptAiExtractionRepository;
    private final ReceiptVerificationRepository receiptVerificationRepository;
    private final ExpenseRepository expenseRepository;
    private final MemberRepository memberRepository;
    private final CustomFileUtil customFileUtil;

    @Override
    public ReceiptDTO upload(Long expenseId, Long userId, MultipartFile file) {
        Expense expense = expenseRepository.findByIdAndWriterId(expenseId, userId)
                .orElseThrow(() -> new RuntimeException("지출 내역을 찾을 수 없습니다."));

        if (!expense.isDraft()) {
            throw new IllegalStateException("DRAFT 상태의 지출 내역에만 영수증을 업로드할 수 있습니다.");
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 파일 유효성 검증
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("파일이 비어 있습니다.");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new RuntimeException("이미지 파일만 업로드할 수 있습니다.");
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new RuntimeException("파일 크기가 너무 큽니다. 10MB 이하만 업로드 가능합니다.");
        }

        // 파일 저장
        String fileUrl = customFileUtil.saveFile(file, "receipts");

        // 파일 해시 생성 (중복 확인용)
        String fileHash = customFileUtil.generateFileHash(file);

        // 중복 업로드 방지: 동일 해시가 이미 존재하면 예외
        if (fileHash != null && receiptUploadRepository.findByFileHash(fileHash).isPresent()) {
            throw new RuntimeException("이미 업로드된 영수증입니다. 같은 영수증을 중복 업로드할 수 없습니다.");
        }

        // 기존 영수증이 있으면 삭제
        receiptUploadRepository.findByExpenseId(expenseId).ifPresent(receiptUploadRepository::delete);

        // ReceiptUpload 저장
        ReceiptUpload receiptUpload = ReceiptUpload.builder()
                .expense(expense)
                .uploadedBy(member)
                .fileUrl(fileUrl)
                .fileHash(fileHash)
                .mimeType(file.getContentType())
                .build();

        ReceiptUpload saved = receiptUploadRepository.save(receiptUpload);

        // TODO: AI 추출 작업은 비동기로 실행 (별도 서비스에서 처리)
        // 여기서는 ReceiptUpload만 저장하고, AI 추출은 별도 프로세스에서 처리

        return entityToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptDTO get(Long id, Long userId) {
        ReceiptUpload receiptUpload = receiptUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));

        // 권한 확인: 본인의 지출 내역인지 확인
        if (!receiptUpload.getExpense().getWriter().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        return entityToDTO(receiptUpload);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getImage(Long id, Long userId) {
        ReceiptUpload receiptUpload = receiptUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));

        // 권한 확인
        if (!receiptUpload.getExpense().getWriter().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        Path filePath = Paths.get(receiptUpload.getFileUrl());
        return customFileUtil.getFileAsResource(filePath);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptExtractionDTO getExtraction(Long id, Long userId) {
        ReceiptUpload receiptUpload = receiptUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));

        // 권한 확인
        if (!receiptUpload.getExpense().getWriter().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        ReceiptAiExtraction extraction = receiptAiExtractionRepository.findByReceiptId(id)
                .orElseThrow(() -> new RuntimeException("AI 추출 결과를 찾을 수 없습니다."));

        return ReceiptExtractionDTO.builder()
                .receiptId(id)
                .modelName(extraction.getModelName())
                .extractedDate(extraction.getExtractedDate())
                .extractedAmount(extraction.getExtractedAmount())
                .extractedMerchant(extraction.getExtractedMerchant())
                .extractedCategory(extraction.getExtractedCategory())
                .confidence(extraction.getConfidence())
                .extractedJson(extraction.getExtractedJson())
                .createdAt(extraction.getCreatedAt())
                .build();
    }

    @Override
    public void remove(Long id, Long userId) {
        ReceiptUpload receiptUpload = receiptUploadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("영수증을 찾을 수 없습니다."));

        // 권한 확인
        if (!receiptUpload.getExpense().getWriter().getId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        // DRAFT 상태인지 확인
        if (!receiptUpload.getExpense().isDraft()) {
            throw new IllegalStateException("DRAFT 상태의 지출 내역에만 영수증을 삭제할 수 있습니다.");
        }

        // 파일 삭제
        Path filePath = Paths.get(receiptUpload.getFileUrl());
        customFileUtil.deleteFile(filePath);

        // ReceiptUpload 삭제 (CASCADE로 ReceiptAiExtraction도 함께 삭제됨)
        receiptUploadRepository.delete(receiptUpload);
    }

    private ReceiptDTO entityToDTO(ReceiptUpload entity) {
        ReceiptDTO.ReceiptDTOBuilder builder = ReceiptDTO.builder()
                .id(entity.getId())
                .expenseId(entity.getExpense().getId())
                .uploadedBy(entity.getUploadedBy().getId())
                .uploadedByName(entity.getUploadedBy().getName())
                .fileUrl(entity.getFileUrl())
                .fileHash(entity.getFileHash())
                .mimeType(entity.getMimeType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // AI 추출 결과 추가
        Optional<ReceiptAiExtraction> extractionOpt = receiptAiExtractionRepository.findByReceiptId(entity.getId());
        if (extractionOpt.isPresent()) {
            ReceiptAiExtraction extraction = extractionOpt.get();
            builder.extractionId(extraction.getId())
                    .modelName(extraction.getModelName())
                    .extractedJson(extraction.getExtractedJson())
                    .extractedDate(extraction.getExtractedDate())
                    .extractedAmount(extraction.getExtractedAmount())
                    .extractedMerchant(extraction.getExtractedMerchant())
                    .extractedCategory(extraction.getExtractedCategory())
                    .confidence(extraction.getConfidence())
                    .extractionCreatedAt(extraction.getCreatedAt());
        }

        // 검증 결과 추가
        Optional<ReceiptVerification> verificationOpt = receiptVerificationRepository.findByExpenseId(entity.getExpense().getId());
        if (verificationOpt.isPresent()) {
            ReceiptVerification verification = verificationOpt.get();
            builder.verificationId(verification.getId())
                    .verifiedBy(verification.getVerifiedBy().getId())
                    .verifiedByName(verification.getVerifiedBy().getName())
                    .verifiedMerchant(verification.getVerifiedMerchant())
                    .verifiedAmount(verification.getVerifiedAmount())
                    .verifiedCategory(verification.getVerifiedCategory())
                    .reason(verification.getReason())
                    .verificationCreatedAt(verification.getCreatedAt());
        }

        return builder.build();
    }
}

