package com.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class DevHeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String userId = req.getHeader("X-User-Id");
        String role = req.getHeader("X-Role");
        String dept = req.getHeader("X-Dept");

        if (userId != null) userId = userId.trim();
        if (role != null) role = role.trim().toUpperCase();
        if (dept != null) dept = dept.trim();

        if (role != null && role.startsWith("ROLE_")) role = role.substring("ROLE_".length());

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && userId != null && !userId.isBlank()
                && role != null && !role.isBlank()) {

            long uid;
            try { uid = Long.parseLong(userId); }
            catch (NumberFormatException e) { chain.doFilter(req,res); return; }

            ReportPrincipal principal = new ReportPrincipal(uid, role, (dept == null || dept.isBlank()) ? null : dept);

            var auth = new UsernamePasswordAuthenticationToken(
                    principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);


    }

        chain.doFilter(req, res);
    }
}
