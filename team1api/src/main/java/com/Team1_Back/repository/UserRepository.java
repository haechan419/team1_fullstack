package com.Team1_Back.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.Team1_Back.domain.User;

// JpaRepository<User, String> : User 엔티티를 관리하는 리포지토리 인터페이스
// String : User 엔티티의 기본 키 타입
@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    // 사번으로 사용자 정보를 조회합니다.
    Optional<User> findByEmployeeNo(String employeeNo);
    
    // 사번 중복 체크합니다 
    boolean existsByEmployeeNo(String employeeNo);


    
}
