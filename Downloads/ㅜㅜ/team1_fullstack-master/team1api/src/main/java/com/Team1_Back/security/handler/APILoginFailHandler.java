
package com.Team1_Back.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.Team1_Back.security.listener.LoginFailEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class APILoginFailHandler implements AuthenticationFailureHandler {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.info("----------- APILoginFailHandler -----------");

        String employeeNo = request.getParameter("employeeNo");
        String ipAddress = getClientIp(request);

        log.info("로그인 실패 - 사번: {}, IP: {}, 원인: {}", employeeNo, ipAddress, exception.getMessage());

        // 이벤트 발행 (실패 횟수 증가, 계정 잠금, 로그인 기록 저장)
        LoginFailEvent event = new LoginFailEvent(employeeNo, ipAddress);
        eventPublisher.publishEvent(event);

        // 이벤트 처리 후 메시지 가져오기
        String errorMessage = event.getErrorMessage();

        // JSON 응답
        Gson gson = new Gson();
        String jsonStr = gson.toJson(Map.of(
                "success", false,
                "error", "ERROR_LOGIN",
                "message", errorMessage
        ));

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);
        printWriter.close();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}