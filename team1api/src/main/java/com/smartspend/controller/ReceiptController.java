package com.smartspend.controller;

import com.smartspend.dto.ReceiptDTO;
import com.smartspend.dto.ReceiptExtractionDTO;
import com.smartspend.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/receipt/receipts")
@Log4j2
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/upload")
    public Map<String, Long> upload(
            @RequestParam("expenseId") Long expenseId,
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("userId") Long userId) {
        ReceiptDTO dto = receiptService.upload(expenseId, userId, file);
        return Map.of("result", dto.getId());
    }

    @GetMapping("/{id}")
    public ReceiptDTO get(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        return receiptService.get(id, userId);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        Resource resource = receiptService.getImage(id, userId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/extraction")
    public ReceiptExtractionDTO getExtraction(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        return receiptService.getExtraction(id, userId);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> remove(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        receiptService.remove(id, userId);
        return Map.of("RESULT", "SUCCESS");
    }
}

