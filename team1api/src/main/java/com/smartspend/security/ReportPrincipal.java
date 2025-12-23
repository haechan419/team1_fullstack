package com.smartspend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

/**
 * Spring Security Principal 구현체
 * JWT 토큰에서 추출한 사용자 정보를 담는 클래스
 */
@Getter
@AllArgsConstructor
public class ReportPrincipal implements Principal {

    private Long userId;
    private String role;
    private String departmentName;

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}

