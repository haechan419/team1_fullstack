package com.smartspend.controller;

import com.smartspend.domain.Member;
import com.smartspend.dto.MemberDTO;
import com.smartspend.repository.MemberRepository;
import com.smartspend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Log4j2
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public MemberDTO getCurrentUser(@RequestAttribute("userId") Long userId) {
        if (userId == null) {
            throw new RuntimeException("인증이 필요합니다.");
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 임시 인증 모드: USER001이면 관리자 권한으로 반환
        // (JwtAuthenticationFilter에서 ADMIN 역할로 설정했으므로 일치시킴)
        String role = member.getRole().name();
        if ("USER001".equals(member.getEmployeeNo()) && "USER".equals(role)) {
            role = "ADMIN"; // 임시 인증 모드에서 관리자 권한으로 설정
            log.warn("⚠️ [임시 인증 모드] USER001을 관리자 권한으로 반환 - 실제 로그인 기능 구현 후 제거 필요");
        }

        return MemberDTO.builder()
                .id(member.getId())
                .employeeNo(member.getEmployeeNo())
                .email(member.getEmail())
                .name(member.getName())
                .departmentName(null) // 부서 기능 미사용
                .role(role)
                .isActive(member.getIsActive())
                .build();
    }

    /**
     * [개발용] 사번으로 토큰 생성 (개발/테스트 환경 전용)
     * 
     * ⚠️ 실제 로그인 기능 구현 후 제거 필요
     * 
     * 실제 로그인 API가 구현되면:
     * 1. 이 메서드 전체를 삭제하거나
     * 2. @Profile("dev") 또는 조건부 활성화로 변경
     * 
     * 실제 로그인 API는 일반적으로:
     * - POST /api/auth/login (email/password로 인증)
     * - POST /api/auth/refresh (토큰 갱신)
     * 
     * 현재 이 API는 개발 중 사용자 전환 테스트용으로만 사용됩니다.
     */
    @PostMapping("/dev/token")
    public Map<String, String> generateDevToken(@RequestParam String employeeNo) {
        Member member = memberRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + employeeNo));

        String token = jwtUtil.generateToken(member.getId(), member.getRole().name());

        log.info("🔑 [개발용] 토큰 생성: {} ({}) - 실제 로그인 기능 구현 후 제거 필요", member.getName(), member.getEmployeeNo());
        return Map.of(
                "token", token,
                "employeeNo", member.getEmployeeNo(),
                "name", member.getName(),
                "role", member.getRole().name()
        );
    }
}

