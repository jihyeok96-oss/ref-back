package com.logic.project.api;

import com.logic.project.domain.Member;
import com.logic.project.domain.MemberRole;
import com.logic.project.domain.Qna;
import com.logic.project.service.QnaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:80",
                "http://127.0.0.1:80"
        },
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        }
)
public class QnaRestController {

    private final QnaService qnaService;


    // =========================================================
    // Q&A 전체 목록
    // =========================================================

    @GetMapping
    public ResponseEntity<?> list(
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        List<Qna> qnas =
                qnaService.findAll();

        return ResponseEntity.ok(qnas);
    }


    // =========================================================
    // 내가 작성한 Q&A
    // =========================================================

    @GetMapping("/mine")
    public ResponseEntity<?> mine(
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        return ResponseEntity.ok(
                qnaService.findMyQna(
                        loginMember.getId()
                )
        );
    }


    // =========================================================
    // Q&A 상세
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(
            @PathVariable Long id,
            HttpSession session
    ) {

        requireLoginMember(session);

        Qna qna =
                qnaService.findById(id);

        return ResponseEntity.ok(qna);
    }


    // =========================================================
    // Q&A 작성
    // =========================================================

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        Qna qna =
                qnaService.create(
                        loginMember.getId(),
                        body.get("title"),
                        body.get("content")
                );

        return ResponseEntity.ok(qna);
    }


    // =========================================================
    // Q&A 수정
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        boolean admin =
                loginMember.getRole() == MemberRole.ADMIN;

        Qna qna =
                qnaService.update(
                        loginMember.getId(),
                        admin,
                        id,
                        body.get("title"),
                        body.get("content")
                );

        return ResponseEntity.ok(qna);
    }


    // =========================================================
    // Q&A 삭제
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        boolean admin =
                loginMember.getRole() == MemberRole.ADMIN;

        qnaService.delete(
                loginMember.getId(),
                admin,
                id
        );

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // 관리자 답변 등록
    // =========================================================

    @PostMapping("/{id}/answer")
    public ResponseEntity<?> answer(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        requireAdmin(loginMember);

        Qna qna =
                qnaService.answer(
                        loginMember.getId(),
                        id,
                        body.get("answer")
                );

        return ResponseEntity.ok(qna);
    }


    // =========================================================
    // 관리자 답변 수정
    // =========================================================

    @PutMapping("/{id}/answer")
    public ResponseEntity<?> updateAnswer(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        requireAdmin(loginMember);

        Qna qna =
                qnaService.updateAnswer(
                        loginMember.getId(),
                        id,
                        body.get("answer")
                );

        return ResponseEntity.ok(qna);
    }


    // =========================================================
    // 관리자 답변 삭제
    // =========================================================

    @DeleteMapping("/{id}/answer")
    public ResponseEntity<?> deleteAnswer(
            @PathVariable Long id,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        requireAdmin(loginMember);

        qnaService.deleteAnswer(
                loginMember.getId(),
                id
        );

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // 로그인 회원 확인
    // =========================================================

    private Member requireLoginMember(
            HttpSession session
    ) {

        Member loginMember =
                (Member) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null) {

            throw new IllegalStateException(
                    "로그인이 필요합니다."
            );
        }

        return loginMember;
    }


    // =========================================================
    // 관리자 권한 확인
    // =========================================================

    private void requireAdmin(
            Member member
    ) {

        if (member.getRole() != MemberRole.ADMIN) {

            throw new SecurityException(
                    "관리자 권한이 필요합니다."
            );
        }
    }
}