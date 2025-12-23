package com.smartspend.service;

import com.smartspend.dto.ReceiptDTO;
import com.smartspend.dto.ReceiptVerificationDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import org.springframework.core.io.Resource;

public interface AdminReceiptService {

    PageResponseDTO<ReceiptDTO> getList(PageRequestDTO pageRequestDTO, String status, Long approverId);

    ReceiptDTO get(Long id);

    Resource getImage(Long id);

    com.smartspend.dto.ReceiptExtractionDTO getExtraction(Long id);

    void verify(Long id, ReceiptVerificationDTO verificationDTO, Long adminId);
}

