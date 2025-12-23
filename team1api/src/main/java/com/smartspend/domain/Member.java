package com.smartspend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user", indexes = {
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_no", length = 50, nullable = false, unique = true)
    private String employeeNo;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private MemberRole role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // 도메인 메서드
    public boolean isAdmin() {
        return MemberRole.ADMIN.equals(this.role);
    }

    public boolean isUser() {
        return MemberRole.USER.equals(this.role);
    }
}

