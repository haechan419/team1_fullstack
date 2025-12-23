package com.smartspend.controller;

import com.smartspend.domain.Member;
import com.smartspend.dto.DepartmentStatisticsDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import com.smartspend.dto.ReportJobDTO;
import com.smartspend.repository.MemberRepository;
import com.smartspend.service.AccountingService;
import com.smartspend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/accounting")
@Log4j2
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;
    private final ReportService reportService;
    private final MemberRepository memberRepository;

    /**
     * 부서별 지출 통계 조회 (mallapi 패턴: 직접 DTO 반환)
     */
    @GetMapping("/statistics/department")
    public List<DepartmentStatisticsDTO> getDepartmentStatistics(
            @RequestParam(value = "status", required = false) String status,
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

        log.info("🔍 부서별 통계 조회 요청 - userId: {}, status: {}", userId, status);
        return accountingService.getDepartmentStatistics(status);
    }

    /**
     * 부서 목록 조회 (mallapi 패턴: 직접 List 반환)
     */
    @GetMapping("/departments")
    public List<String> getDepartments(@RequestAttribute("userId") Long userId) {

        // 관리자 권한 체크
        Member admin = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
        
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }

        log.info("🔍 부서 목록 조회 요청 - userId: {}", userId);
        return accountingService.getDepartments();
    }

    /**
     * 카테고리별 통계 조회 (mallapi 패턴: 직접 List 반환)
     */
    @GetMapping("/statistics/category")
    public List<Map<String, Object>> getCategoryStatistics(
            @RequestParam(value = "status", required = false) String status,
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

        log.info("🔍 카테고리별 통계 조회 요청 - userId: {}, status: {}", userId, status);
        return accountingService.getCategoryStatistics(status);
    }

    /**
     * 전체 통계 요약 조회 (mallapi 패턴: 직접 Map 반환)
     */
    @GetMapping("/statistics/summary")
    public Map<String, Object> getSummary(@RequestAttribute("userId") Long userId) {

        // 관리자 권한 체크
        Member admin = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
        
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }

        log.info("🔍 전체 통계 요약 조회 요청 - userId: {}", userId);
        return accountingService.getSummary();
    }

    /**
     * 예산 초과 인원 리스트 조회 (mallapi 패턴: 직접 List 반환)
     */
    @GetMapping("/statistics/over-budget")
    public List<Map<String, Object>> getOverBudgetList(@RequestAttribute("userId") Long userId) {

        // 관리자 권한 체크
        Member admin = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = admin.isAdmin() || "USER001".equals(admin.getEmployeeNo());
        
        if (!isAdmin) {
            log.warn("❌ 관리자 권한이 없습니다. userId: {}", userId);
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }

        log.info("🔍 예산 초과 인원 리스트 조회 요청 - userId: {}", userId);
        return accountingService.getOverBudgetList();
    }

    /**
     * 리포트 생성 (mallapi 패턴: 직접 Map 반환)
     */
    @PostMapping("/reports")
    public Map<String, Long> generateReport(
            @RequestBody ReportJobDTO reportJobDTO,
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

        log.info("🔍 리포트 생성 요청 - userId: {}, reportType: {}", userId, reportJobDTO.getReportType());
        Long reportJobId = reportService.generateReport(reportJobDTO, userId);
        return Map.of("id", reportJobId);
    }

    /**
     * 리포트 목록 조회 (mallapi 패턴: 직접 PageResponseDTO 반환)
     */
    @GetMapping("/reports")
    public PageResponseDTO<ReportJobDTO> getReportList(
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

        log.info("🔍 리포트 목록 조회 요청 - userId: {}", userId);
        return reportService.getReportList(userId, pageRequestDTO);
    }

    /**
     * 리포트 다운로드 (mallapi 패턴: ResponseEntity<Resource> 반환)
     */
    @GetMapping("/reports/{id}/download")
    public ResponseEntity<Resource> downloadReport(
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

        log.info("🔍 리포트 다운로드 요청 - userId: {}, reportId: {}", userId, id);
        byte[] fileData = reportService.downloadReport(id, userId);
        ReportJobDTO reportJob = reportService.getReport(id, userId);

        ByteArrayResource resource = new ByteArrayResource(fileData);
        String contentType = "EXCEL".equals(reportJob.getTemplate()) 
            ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            : "application/pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + reportJob.getFileName() + "\"")
                .body(resource);
    }
}

