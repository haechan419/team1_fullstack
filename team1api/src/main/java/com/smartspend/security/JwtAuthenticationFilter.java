package com.smartspend.security;

import com.smartspend.domain.Member;
import com.smartspend.repository.MemberRepository;
import com.smartspend.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정하는 필터
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                Long userId = jwtUtil.validateAndExtractUserId(token);
                String role = jwtUtil.extractRole(token);

                if (userId != null && role != null) {
                    // ReportPrincipal 생성 (userId, role)
                    // 부서 기능은 사용하지 않으므로 departmentName은 null로 설정
                    ReportPrincipal principal = new ReportPrincipal(userId, role, null);

                    // Authority 생성 (role 필수 - 팀원 요청사항)
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );

                    // SecurityContext에 인증 정보 설정
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // RequestAttribute에도 userId 설정 (기존 코드와의 호환성)
                    request.setAttribute("userId", userId);
                }
            } catch (Exception e) {
                log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
                // 인증 실패 시 SecurityContext는 비워둠
            }
        } else {
            // ============================================================
            // [임시 인증 모드] 로그인 기능 구현 전 개발용 코드
            // ============================================================
            // ⚠️ 실제 로그인 기능 구현 후 이 블록 전체를 제거해야 합니다.
            // 
            // 제거 시점:
            // - 실제 로그인 API (POST /api/auth/login) 구현 완료 후
            // - 프론트엔드에서 로그인 페이지가 정상 작동할 때
            //
            // 제거 방법:
            // 1. 이 else 블록 전체 삭제
            // 2. 토큰이 없을 때는 인증 실패로 처리 (401 Unauthorized)
            // 3. SecurityConfig에서 인증이 필요한 API는 .authenticated()로 변경
            // ============================================================
            try {
                Member defaultUser = memberRepository.findByEmployeeNo("USER001").orElse(null);
                if (defaultUser != null) {
                    Long userId = defaultUser.getId();
                    // 임시 인증 모드에서는 관리자 권한으로 설정 (로그인 기능 구현 전까지)
                    String role = "ADMIN"; // USER001을 관리자로 임시 설정

                    // ReportPrincipal 생성 (부서 기능 미사용)
                    ReportPrincipal principal = new ReportPrincipal(userId, role, null);
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    request.setAttribute("userId", userId);

                    log.warn("⚠️ [임시 인증 모드] USER001로 자동 로그인 (userId: {}, role: ADMIN) - 실제 로그인 기능 구현 후 제거 필요", userId);
                } else {
                    log.error("❌ USER001을 찾을 수 없습니다. 데이터베이스를 확인하세요.");
                }
            } catch (Exception e) {
                log.error("❌ 임시 인증 설정 실패: {}", e.getMessage(), e);
                // 예외가 발생해도 필터 체인은 계속 진행
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     * Authorization: Bearer {token} 형식
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

