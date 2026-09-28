package com.logic.project.controller;

import com.logic.project.domain.Member;
import com.logic.project.domain.MemberRole;
import com.logic.project.domain.Qna;
import com.logic.project.service.QnaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/qna")
public class QnaController {

    private final QnaService qnaService;

    /*
     * =========================================================
     * Q&A 목록
     * =========================================================
     *
     * GET /qna
     *
     * 로그인한 회원만 Q&A 목록을 볼 수 있다.
     */
    @GetMapping
    public String list(
            HttpSession session,
            Model model
    ) {

        Member loginMember =
                requireLoginMember(session);

        model.addAttribute(
                "qnas",
                qnaService.findAll()
        );

        /*
         * 현재 로그인 회원 정보를 필요하면
         * Thymeleaf에서 사용할 수 있도록 전달한다.
         */
        model.addAttribute(
                "loginMember",
                loginMember
        );

        return "qna/list";
    }

    /*
     * =========================================================
     * Q&A 작성 화면
     * =========================================================
     *
     * GET /qna/new
     */
    @GetMapping("/new")
    public String newForm(
            HttpSession session
    ) {

        requireLoginMember(session);

        return "qna/form";
    }

    /*
     * =========================================================
     * Q&A 작성
     * =========================================================
     *
     * POST /qna
     */
    @PostMapping
    public String create(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        qnaService.create(
                loginMember.getId(),
                title,
                content
        );

        return "redirect:/qna";
    }

    /*
     * =========================================================
     * Q&A 상세
     * =========================================================
     *
     * GET /qna/{id}
     */
    @GetMapping("/{id}")
    public String detail(
            @PathVariable("id") Long id,
            HttpSession session,
            Model model
    ) {

        Member loginMember =
                requireLoginMember(session);

        Qna qna =
                qnaService.findById(id);

        model.addAttribute(
                "qna",
                qna
        );

        model.addAttribute(
                "loginMember",
                loginMember
        );

        /*
         * Thymeleaf에서
         *
         * ${isAdmin}
         *
         * 으로 관리자 여부를 확인할 수 있도록 한다.
         */
        model.addAttribute(
                "isAdmin",
                loginMember.getRole() == MemberRole.ADMIN
        );

        return "qna/detail";
    }

    /*
     * =========================================================
     * Q&A 수정 화면
     * =========================================================
     *
     * GET /qna/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable("id") Long id,
            HttpSession session,
            Model model
    ) {

        Member loginMember =
                requireLoginMember(session);

        Qna qna =
                qnaService.findById(id);

        model.addAttribute(
                "qna",
                qna
        );

        model.addAttribute(
                "loginMember",
                loginMember
        );

        model.addAttribute(
                "isAdmin",
                loginMember.getRole() == MemberRole.ADMIN
        );

        return "qna/form";
    }

    /*
     * =========================================================
     * Q&A 수정
     * =========================================================
     *
     * POST /qna/{id}/edit
     *
     * 본인 글 또는 관리자만 수정할 수 있다.
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable("id") Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        boolean admin =
                loginMember.getRole() == MemberRole.ADMIN;

        qnaService.update(
                loginMember.getId(),
                admin,
                id,
                title,
                content
        );

        return "redirect:/qna/" + id;
    }

    /*
     * =========================================================
     * Q&A 삭제
     * =========================================================
     *
     * POST /qna/{id}/delete
     *
     * 본인 글 또는 관리자만 삭제할 수 있다.
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable("id") Long id,
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

        return "redirect:/qna";
    }

    /*
     * =========================================================
     * 관리자 답변 작성
     * =========================================================
     *
     * POST /qna/{id}/answer
     */
    @PostMapping("/{id}/answer")
    public String answer(
            @PathVariable("id") Long id,
            @RequestParam("answer") String answer,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        /*
         * 관리자만 답변할 수 있도록
         * Controller에서도 1차적으로 확인한다.
         *
         * 실제 권한 검사는 Service에서도 수행한다.
         */
        requireAdmin(loginMember);

        qnaService.answer(
                loginMember.getId(),
                id,
                answer
        );

        return "redirect:/qna/" + id;
    }

    /*
     * =========================================================
     * 관리자 답변 삭제
     * =========================================================
     *
     * POST /qna/{id}/answer/delete
     */
    @PostMapping("/{id}/answer/delete")
    public String deleteAnswer(
            @PathVariable("id") Long id,
            HttpSession session
    ) {

        Member loginMember =
                requireLoginMember(session);

        /*
         * 관리자만 답변을 삭제할 수 있다.
         */
        requireAdmin(loginMember);

        qnaService.deleteAnswer(
                loginMember.getId(),
                id
        );

        return "redirect:/qna/" + id;
    }

    /*
     * =========================================================
     * 로그인 회원 가져오기
     * =========================================================
     *
     * 현재 프로젝트의 로그인 방식은
     *
     * session.setAttribute("loginMember", member);
     *
     * 이므로 반드시 같은 키를 사용해야 한다.
     */
    private Member requireLoginMember(
            HttpSession session
    ) {

        Member loginMember =
                (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            throw new IllegalStateException(
                    "로그인이 필요합니다."
            );
        }

        return loginMember;
    }

    /*
     * =========================================================
     * 관리자 확인
     * =========================================================
     */
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