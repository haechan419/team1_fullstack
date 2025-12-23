package com.smartspend.controller;

import com.smartspend.domain.Member;
import com.smartspend.dto.ApprovalActionDTO;
import com.smartspend.dto.ApprovalActionLogDTO;
import com.smartspend.dto.ApprovalRequestDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import com.smartspend.repository.MemberRepository;
import com.smartspend.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/approval-requests")
@Log4j2
@RequiredArgsConstructor
public class ApprovalRequestController {

    private final ApprovalService approvalService;
    private final MemberRepository memberRepository;

    @GetMapping("/list")
    public PageResponseDTO<ApprovalRequestDTO> getList(
            @RequestParam(value = "requestType", required = false) String requestType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            PageRequestDTO pageRequestDTO,
            @RequestAttribute("userId") Long userId) {
        
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = member.isAdmin() || "USER001".equals(member.getEmployeeNo());

        return approvalService.getList(userId, isAdmin, pageRequestDTO, requestType, status, startDate, endDate);
    }

    @GetMapping("/{id}")
    public ApprovalRequestDTO get(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = member.isAdmin() || "USER001".equals(member.getEmployeeNo());

        return approvalService.get(id, userId, isAdmin);
    }

    @GetMapping("/{id}/logs")
    public List<ApprovalActionLogDTO> getLogs(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = member.isAdmin() || "USER001".equals(member.getEmployeeNo());

        return approvalService.getLogs(id, userId, isAdmin);
    }

    @PutMapping("/{id}/action")
    public java.util.Map<String, String> action(
            @PathVariable Long id,
            @RequestBody ApprovalActionDTO actionDTO,
            @RequestAttribute("userId") Long adminId) {
        
        // 관리자 권한 체크
        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
        
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", adminId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        approvalService.action(id, actionDTO, adminId);
        return java.util.Map.of("RESULT", "SUCCESS");
    }

    // 지출 결재 목록 조회 (하위 경로 - 경로 충돌 방지)
    @GetMapping("/types/expense")
    public PageResponseDTO<ApprovalRequestDTO> getExpenseList(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            PageRequestDTO pageRequestDTO,
            @RequestAttribute("userId") Long userId) {
        
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = member.isAdmin() || "USER001".equals(member.getEmployeeNo());
        
        // 관리자 전용 API이므로 권한 체크
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }

        return approvalService.getList(userId, isAdmin, pageRequestDTO, "EXPENSE", status, startDate, endDate);
    }

    // 비품 결재 목록 조회 (하위 경로, 향후 구현)
    @GetMapping("/types/product")
    public PageResponseDTO<ApprovalRequestDTO> getProductList(
            @RequestParam(value = "status", required = false) String status,
            PageRequestDTO pageRequestDTO,
            @RequestAttribute("userId") Long userId) {

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = member.isAdmin() || "USER001".equals(member.getEmployeeNo());

        // 관리자 전용 API이므로 권한 체크
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }

        // mallapi 패턴: 서비스 레이어에 필터/페이징 위임
        // 비품 결재는 아직 상신일 필터를 사용하지 않으므로 startDate/endDate 는 null 전달
        return approvalService.getList(userId, isAdmin, pageRequestDTO, "STORE_ORDER", status, null, null);
    }
}

