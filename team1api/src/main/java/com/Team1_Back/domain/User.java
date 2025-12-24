package com.Team1_Back.domain;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;



import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // id : 고유 번호 (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // name = "" : 은 데이터베이스에 저장될때 이름을 정해주는것. 없으면 자바변수 그대로이름이 저장된다.
    // unique = true : 중복 방지 
    // nullable = false : 비워지면 안되는 데아터
    @Column(name = "employee_no", nullable = false, unique = true, length = 50)
    private String employeeNo;  // 사번 (로그인 ID)

    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;  // bcrypt 암호화

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "department_name", length = 100)
    private String departmentName;  // 부서명

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;  // USER / ADMIN

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;  // 재직 여부

    @Column(name = "failed_login_count", nullable = false)
    @Builder.Default
    private Integer failedLoginCount = 0;  // 로그인 실패 횟수

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;  // 계정 잠금 시각

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // 퇴사 처리 시각

    // CreationTimestamp : 데이터가 생성될 때 자동으로 시간이 들어간다.
    @CreationTimestamp
    @Column(name = "created_user_at", nullable = false, updatable = false)
    private LocalDateTime createdUserAt;

    // UpdateTimestamp : 데이터가 수정될 때 자동으로 시간이 들어간다.
    @UpdateTimestamp
    @Column(name = "updated_user_at", nullable = false)
    private LocalDateTime updatedUserAt;


    // Service 에서 사용하는 메서드 정리

    // 계정 잠금 여부 확인
    public boolean isLocked() {
        return this.lockedAt != null;
    }

    // 퇴사 여부를 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // 로그인 실패 시 로그인 실패 횟수를 증가시킨다.
    public void increaseFailedLoginCount() {
        this.failedLoginCount++;
    }

    // 로그인 실패 횟수를 초기화한다.(로그인 성공 시)
    public void resetFailedLoginCount() {
        this.failedLoginCount = 0;
    }

    // 계정 잠금 시 계정 잠금 시각을 저장한다.
    public void lock() {
        this.lockedAt = LocalDateTime.now();
    }

    // 계정 잠금 해제 시 로그인 실패 횟수를 초기화한다.
    public void unlock() {
        this.lockedAt = null;
        this.failedLoginCount = 0;
    }
}