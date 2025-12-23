package com.Team1_Back.repository;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Team1_Back.domain.Role;
import com.Team1_Back.domain.User;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@Slf4j
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testUserInsert() {
        User user = User.builder()
                .employeeNo("11111113")
                .name("관리자")
                .email("test@aaa.com")
                .password(passwordEncoder.encode("1111"))
                .role(Role.ADMIN)
                .departmentName("관리자")
                .isActive(true)
                .failedLoginCount(0)
                .lockedAt(null)
                .deletedAt(null)
                .build();  // createdUserAt, updatedUserAt 제거 (자동 생성됨)

        userRepository.save(user);
        log.info("User saved: " + user.getId());
    }
}