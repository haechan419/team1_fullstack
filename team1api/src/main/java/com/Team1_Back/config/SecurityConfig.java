package com.Team1_Back.config;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.Team1_Back.security.handler.APILoginFailHandler;
import com.Team1_Back.security.handler.APILoginSuccessHandler;

import com.Team1_Back.config.DevHeaderAuthFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final ApplicationEventPublisher eventPublisher;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ CORS: 한 군데에서만 관리(중복 금지)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ⚠️ allowCredentials(true)면 "*" 불가 → 정확한 origin 나열
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"   // Vite 쓰면 보통 이쪽도 필요
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Content-Type",
                "Authorization",
                "Cache-Control",
                "X-User-Id",
                "X-Role",
                "X-Dept"
        ));

        // 다운로드 파일명 등 프론트에서 읽어야 하면
        config.setExposedHeaders(List.of("Content-Disposition"));

        // ✅ formLogin(세션/쿠키)면 보통 true 필요
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("--------------------- security config (merged) ---------------------");

        http
                .csrf(csrf -> csrf.disable())

                // ✅ 중요: Security에 CORS 적용(위 bean 사용)
                .cors(Customizer.withDefaults())

                // ✅ DevHeaderAuthFilter: 로그인필터보다 먼저
                .addFilterBefore(new DevHeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // ✅ preflight 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ 인증/로그인/로그아웃 엔드포인트는 열어둠
                        .requestMatchers("/api/auth/**").permitAll()

                        // (선택) 디버그용 열기
                        .requestMatchers("/api/reports/debug/**").permitAll()

                        // ✅ 보고서 API는 인증 필요
                        .requestMatchers("/api/reports/**").authenticated()

                        // 나머지는 정책에 맞게:
                        .anyRequest().authenticated()
                )

                // ✅ 폼 로그인(API 로그인 엔드포인트로 사용)
                .formLogin(form -> form
                        .loginPage("/api/auth/login")
                        .usernameParameter("employeeNo")
                        .passwordParameter("password")
                        .successHandler(new APILoginSuccessHandler(eventPublisher))
                        .failureHandler(new APILoginFailHandler(eventPublisher))
                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.setContentType("application/json; charset=UTF-8");
                            response.getWriter().write("{\"success\":true,\"message\":\"로그아웃 성공\"}");
                        })
                )

                // HTTP Basic 비활성화
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
