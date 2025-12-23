package com.smartspend.repository;

import com.smartspend.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmployeeNo(String employeeNo);

    Optional<Member> findByEmail(String email);

    // 부서 목록 조회 (mallapi 패턴: native query 사용)
    @Query(value = 
        "SELECT DISTINCT department_name " +
        "FROM user " +
        "WHERE department_name IS NOT NULL " +
        "ORDER BY department_name",
        nativeQuery = true)
    List<String> findDistinctDepartmentNames();
}

