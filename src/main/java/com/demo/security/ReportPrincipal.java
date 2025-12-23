package com.demo.security;

public record ReportPrincipal(
        Long userId,
        String role,            // USER / ADMIN
        String departmentName   // 예: 마케팅팀
) {}
