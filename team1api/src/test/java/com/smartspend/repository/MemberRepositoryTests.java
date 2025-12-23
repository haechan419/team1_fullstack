package com.smartspend.repository;

import com.smartspend.domain.Member;
import com.smartspend.domain.MemberRole;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
public class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    public void testFindByEmployeeNo() {
        // given
        String employeeNo = "USER001";

        // when
        Optional<Member> result = memberRepository.findByEmployeeNo(employeeNo);

        // then
        assertTrue(result.isPresent());
        assertEquals(employeeNo, result.get().getEmployeeNo());
        log.info("조회된 사용자: {}", result.get());
    }

    @Test
    @Transactional
    public void testFindByEmail() {
        // given
        // 실제 DB에 존재하는 이메일 사용
        Optional<Member> testMember = memberRepository.findByEmployeeNo("USER001");
        if (testMember.isEmpty()) {
            log.warn("테스트 사용자가 없어 테스트를 건너뜁니다.");
            return;
        }
        String email = testMember.get().getEmail();

        // when
        Optional<Member> result = memberRepository.findByEmail(email);

        // then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        log.info("이메일로 조회된 사용자: {}", result.get());
    }

    @Test
    @Transactional
    public void testFindDistinctDepartmentNames() {
        // when
        List<String> departments = memberRepository.findDistinctDepartmentNames();

        // then
        assertNotNull(departments);
        log.info("부서 목록 개수: {}", departments.size());
        departments.forEach(dept -> log.info("부서: {}", dept));
    }

    @Test
    @Transactional
    public void testInsert() {
        // given
        Member member = Member.builder()
                .employeeNo("TEST001")
                .email("test001@test.com")
                .name("테스트 사용자")
                .role(MemberRole.USER)
                .isActive(true)
                .build();

        // when
        Member saved = memberRepository.save(member);

        // then
        assertNotNull(saved.getId());
        assertEquals("TEST001", saved.getEmployeeNo());
        assertEquals("테스트 사용자", saved.getName());
        log.info("저장된 사용자: {}", saved);
    }

    @Test
    @Transactional
    public void testFindById() {
        // given
        Optional<Member> testMember = memberRepository.findByEmployeeNo("USER001");
        if (testMember.isEmpty()) {
            log.warn("테스트 사용자가 없어 테스트를 건너뜁니다.");
            return;
        }
        Long memberId = testMember.get().getId();

        // when
        Optional<Member> result = memberRepository.findById(memberId);

        // then
        assertTrue(result.isPresent());
        assertEquals(memberId, result.get().getId());
        log.info("ID로 조회된 사용자: {}", result.get());
    }
}

