package com.smartspend.controller;

import com.smartspend.domain.Member;
import com.smartspend.dto.ExpenseDTO;
import com.smartspend.dto.ExpenseSubmitDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;
import com.smartspend.repository.MemberRepository;
import com.smartspend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/receipt/expenses")
@Log4j2
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final MemberRepository memberRepository;

    @GetMapping("/list")
    public PageResponseDTO<ExpenseDTO> getList(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            PageRequestDTO pageRequestDTO,
            @RequestAttribute(value = "userId", required = false) Long userId) {

        if (userId == null) {
            log.error("❌ userId가 null입니다. JwtAuthenticationFilter에서 userId를 설정하지 못했습니다.");
            throw new RuntimeException("인증이 필요합니다.");
        }

        log.info("🔍 지출 목록 조회 요청 - userId: {}, status: {}, startDate: {}, endDate: {}", userId, status, startDate, endDate);
        PageResponseDTO<ExpenseDTO> response = expenseService.getList(userId, pageRequestDTO, status, startDate, endDate);
        log.info("✅ 지출 목록 조회 결과 - 총 {}건", response.getDtoList().size());
        return response;
    }

    @GetMapping("/{id}")
    public ExpenseDTO get(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // [임시 인증 모드] USER001을 관리자로 처리 (실제 로그인 기능 구현 후 제거)
        boolean isAdmin = member.isAdmin() || "USER001".equals(member.getEmployeeNo());
        
        return expenseService.get(id, userId, isAdmin);
    }

    @PostMapping("/")
    public Map<String, Long> register(@RequestBody ExpenseDTO expenseDTO, @RequestAttribute("userId") Long userId) {
        Long id = expenseService.register(expenseDTO, userId);
        return Map.of("result", id);
    }

    @PutMapping("/{id}")
    public Map<String, String> modify(
            @PathVariable Long id,
            @RequestBody ExpenseDTO expenseDTO,
            @RequestAttribute("userId") Long userId) {
        expenseDTO.setId(id);
        expenseService.modify(expenseDTO, userId);
        return Map.of("RESULT", "SUCCESS");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> remove(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        expenseService.remove(id, userId);
        return Map.of("RESULT", "SUCCESS");
    }

    @PostMapping("/{id}/submit")
    public Map<String, String> submit(
            @PathVariable Long id,
            @RequestBody(required = false) ExpenseSubmitDTO submitDTO,
            @RequestAttribute("userId") Long userId) {
        if (submitDTO == null) {
            submitDTO = new ExpenseSubmitDTO();
        }
        expenseService.submit(id, userId, submitDTO);
        return Map.of("RESULT", "SUCCESS");
    }
}

