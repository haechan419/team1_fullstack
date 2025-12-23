package com.demo.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ 프론트 주소(3000) 허용
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // ✅ 허용 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ 허용 헤더: 커스텀 헤더들 꼭 넣기
        config.setAllowedHeaders(List.of(
                "Content-Type",
                "Authorization",
                "X-User-Id",
                "X-Role",
                "X-Dept"
        ));

        // (선택) 브라우저가 파일명 헤더 등을 읽게 하고 싶으면 expose
        config.setExposedHeaders(List.of("Content-Disposition"));

        // credentials 안 쓰면 false 유지(보통 dev header는 credentials 불필요)
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
