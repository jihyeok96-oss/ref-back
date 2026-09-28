package com.logic.project.service;

import com.logic.project.domain.Member;
import com.logic.project.domain.MemberRole;
import com.logic.project.domain.Qna;
import com.logic.project.repository.MemberRepository;
import com.logic.project.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {
    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    //전체 질문 및 답변의 목록
    public List<Qna> findAll(){
        return qnaRepository.findAllByOrderByCreatedAtDesc();
    }

    //특정 회원의 질문 및 답변 목록
    public List<Qna> findMyQna(Long memberId) {
        return qnaRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    //질문 및 답변 상세보기 - 옵셔널 처리
    public Qna findById(Long id) {
        return qnaRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("해당 Q&A를 찾을 수 없습니다."));
    }

    //질문 등록 - 회원만 가능하므로 회원인지 여부부터 판단
    @Transactional
    public Qna create(Long memberId, String title, String content) {
        Member member = getMember(memberId);
        Qna qna = Qna.builder()
                .member(member)
                .title(requireText(title, "제목"))
                .content(requireText(content, "내용"))
                .build();
        return qnaRepository.save(qna);
    }

    //자신이 작성하거나 관리자만 질문 수정하기
    @Transactional
    public Qna update(Long memberId, boolean admin, Long qnaId,
                      String title, String content) {
        Qna qna = findById(qnaId);
        checkOwnerOrAdmin(qna, memberId, admin);
        qna.updateQuestion(
                requireText(title, "제목"),
                requireText(content, "내용")
        );
        return qna;
    }

    //자신이 작성한 질문이거나 관리자만 삭제하기
    @Transactional
    public void delete(Long memberId, boolean admin, Long qnaId) {
        Qna qna = findById(qnaId);
        checkOwnerOrAdmin(qna, memberId, admin);
        qnaRepository.delete(qna);
    }

    //답변하기(관리자만)
    @Transactional
    public Qna answer(Long memberId, Long qnaId, String answer) {
        Member member = getMember(memberId);
        checkAdmin(member);
        Qna qna = findById(qnaId);
        qna.writeAnswer(requireText(answer, "답변"));
        return qna;
    }

    //답변 수정(관리자만)
    @Transactional
    public Qna updateAnswer(Long memberId, Long qnaId, String answer) {
        return answer(memberId, qnaId, answer);
    }

    //답변 삭제(관리자만)
    @Transactional
    public void deleteAnswer(Long memberId, Long qnaId) {
        Member member = getMember(memberId);
        checkAdmin(member);
        Qna qna = findById(qnaId);
        qna.deleteAnswer();
    }

    //회원 여부 판단 - 옵셔널 처리
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
                );
    }

    //관리자 계정 여부 판단
    private void checkAdmin(Member member) {
        if(member.getRole() != MemberRole.ADMIN) {
            throw new SecurityException("관리자 권한이 필요합니다.");
        }
    }

    //본인이 작성한 Qna이거나 관리자인지 여부 판단
    private void checkOwnerOrAdmin(Qna qna, Long memberId, boolean admin) {
        if(!admin && !qna.getMember().getId().equals(memberId)) {
            throw new SecurityException(
                    "본인이 작성한 Q&A만 수정하거나 삭제할 수 있습니다."
            );
        }
    }

    //빈 값 또는 널 여부 판단
    private String requireText(String value, String fieldName) {
        if(value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }

}
