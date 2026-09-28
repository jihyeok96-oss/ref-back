package com.logic.project.repository;

import com.logic.project.domain.Member;
import com.logic.project.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findByEmail(String email);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    List<Member> findAllByOrderByIdDesc();
    List<Member> findByStatusOrderByIdDesc(MemberStatus status);
    List<Member> findTop5ByOrderByCreatedAtDesc();
}
