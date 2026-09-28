package com.logic.project.repository;

import com.logic.project.domain.Qna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QnaRepository extends JpaRepository<Qna, Long> {
    List<Qna> findAllByOrderByCreatedAtDesc();
    List<Qna> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
