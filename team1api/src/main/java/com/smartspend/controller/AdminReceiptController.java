package com.smartspend.controller;

import com.smartspend.domain.Member;
import com.smartspend.dto.ReceiptDTO;
import com.smartspend.dto.ReceiptVerificationDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import com.smartspend.repository.MemberRepository;
import com.smartspend.service.AdminReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/receipts")
@Log4j2
@RequiredArgsConstructor
public class AdminReceiptController {

    private final AdminReceiptService adminReceiptService;
    private final MemberRepository memberRepository;

    @GetMapping("/list")
    public PageResponseDTO<ReceiptDTO> getList(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "approverId", required = false) Long approverId,
            PageRequestDTO pageRequestDTO,
            @RequestAttribute("userId") Long userId) {
        
        // 관리자 권한 체크
        Member admin = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
        
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        log.info("🔍 관리자 영수증 목록 조회 요청 - status: {}, approverId: {}", status, approverId);
        PageResponseDTO<ReceiptDTO> response = adminReceiptService.getList(pageRequestDTO, status, approverId);
        log.info("✅ 관리자 영수증 목록 조회 결과 - 총 {}건", response.getDtoList().size());
        return response;
    }

    @GetMapping("/{id}")
    public ReceiptDTO get(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        
        // 관리자 권한 체크
        Member admin = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
        
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        return adminReceiptService.get(id);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        
        // 관리자 권한 체크
        Member admin = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
        
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Resource resource = adminReceiptService.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/extraction")
    public com.smartspend.dto.ReceiptExtractionDTO getExtraction(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        
        // 관리자 권한 체크
        Member admin = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
        
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        return adminReceiptService.getExtraction(id);
    }

    @PutMapping("/{id}/verify")
    public Map<String, String> verify(
            @PathVariable(required = false) Long id,
            @RequestBody ReceiptVerificationDTO verificationDTO,
            @RequestAttribute("userId") Long adminId) {
        
        // 관리자 권한 체크
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        if (!admin.isAdmin()) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", adminId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        adminReceiptService.verify(id, verificationDTO, adminId);
        return Map.of("RESULT", "SUCCESS");
    }
    
    // 영수증이 없는 경우를 위한 별도 엔드포인트
    @PutMapping("/expense/{expenseId}/verify")
    public Map<String, String> verifyByExpenseId(
            @PathVariable Long expenseId,
            @RequestBody ReceiptVerificationDTO verificationDTO,
            @RequestAttribute("userId") Long adminId) {
        
        // 관리자 권한 체크
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        if (!admin.isAdmin()) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", adminId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        verificationDTO.setExpenseId(expenseId);
        adminReceiptService.verify(null, verificationDTO, adminId);
        return Map.of("RESULT", "SUCCESS");
    }
}

