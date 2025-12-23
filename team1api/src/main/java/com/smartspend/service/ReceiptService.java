package com.smartspend.service;

import com.smartspend.dto.ReceiptDTO;
import com.smartspend.dto.ReceiptExtractionDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {

    ReceiptDTO upload(Long expenseId, Long userId, MultipartFile file);

    ReceiptDTO get(Long id, Long userId);

    Resource getImage(Long id, Long userId);

    ReceiptExtractionDTO getExtraction(Long id, Long userId);

    void remove(Long id, Long userId);
}

