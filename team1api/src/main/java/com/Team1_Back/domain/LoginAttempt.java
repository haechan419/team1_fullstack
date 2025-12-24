package com.Team1_Back.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "login_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    // 로그인 시도 기록을 저장하는 엔티티

    // id : 고유 번호 (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 사용자가 퇴사를 해도 로그인 시도 기록을 보존하기 위해 사번을 저장한다.
    // 외래키 관계가 아니라 단순히 로그인 시도 기록을 보존하기 위해 사용한다.
    @Column(name = "employee_no", nullable = false, length = 50)    
    private String employeeNo; // 시도한 사번 (로그인 ID)

    @Column(name = "ip_address", length = 45)
    private String ipAddress; // 접속 IP 주소

    @Column(nullable = false)
    private Boolean success; // 성공 여부

    // 생성 시간만 저장하고 수정 불가능 (update = false)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 시도 시각
}
